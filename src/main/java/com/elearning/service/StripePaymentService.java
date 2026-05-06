package com.elearning.service;

import com.elearning.dao.StripeTransactionDAO;
import com.elearning.dao.UserDAO;
import com.elearning.entity.StripeTransaction;
import com.elearning.entity.User;
import com.elearning.util.ApiConfigUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * StripePaymentService — Logique métier pour les paiements Stripe.
 * 
 * Responsabilités :
 * - Créer des sessions de checkout
 * - Mettre à jour les soldes de portefeuille après paiement
 * - Gérer les transactions
 * - Valider les montants
 */
public class StripePaymentService {
    
    private static final Logger logger = Logger.getLogger(StripePaymentService.class.getName());
    
    private final StripeTransactionDAO transactionDAO;
    private final UserDAO userDAO;
    private final UserService userService;
    private final ApiConfigUtil configUtil;

    // Mapping crédit -> centimes (prix en USD)
    // Ajustez selon vos tarifs réels
    public static final Map<Integer, Integer> CREDIT_PRICING = new HashMap<>();
    
    static {
        CREDIT_PRICING.put(10, 999);      // 10 crédits = $9.99 (999 cents)
        CREDIT_PRICING.put(25, 2499);     // 25 crédits = $24.99 (2499 cents)
        CREDIT_PRICING.put(50, 4899);     // 50 crédits = $48.99 (4899 cents)
        CREDIT_PRICING.put(100, 8999);    // 100 crédits = $89.99 (8999 cents)
    }

    public StripePaymentService() {
        this.transactionDAO = new StripeTransactionDAO();
        this.userDAO = new UserDAO();
        this.userService = new UserService();
        this.configUtil = ApiConfigUtil.getInstance();
        
        // Initialiser la clé secrète Stripe
        initializeStripe();
    }

    /**
     * Initialise la clé API Stripe.
     */
    private void initializeStripe() {
        try {
            String secretKey = configUtil.getStripeSecretKey();
            Stripe.apiKey = secretKey;
            logger.info("Stripe API initialisée avec succès");
        } catch (IllegalArgumentException e) {
            logger.severe("Erreur : Clé API Stripe non configurée");
            throw new RuntimeException("Stripe API key not configured", e);
        }
    }

    /**
     * Crée une session de checkout Stripe pour l'achat de crédits.
     * 
     * @param userId ID de l'utilisateur
     * @param credits Nombre de crédits à acheter
     * @param successUrl URL de redirection après succès
     * @param cancelUrl URL de redirection après annulation
     * @return Session Stripe avec l'ID de session
     */
    public StripeCheckoutResponse createCheckoutSession(int userId, int credits, 
                                                         String successUrl, String cancelUrl) 
            throws Exception {
        
        // Valider l'utilisateur
        User user = userDAO.trouverParId(userId);
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé : " + userId);
        }

        // Valider le montant de crédits
        if (!CREDIT_PRICING.containsKey(credits)) {
            throw new IllegalArgumentException("Montant de crédits non valide : " + credits + 
                                             ". Options : " + CREDIT_PRICING.keySet());
        }

        int amountCents = CREDIT_PRICING.get(credits);
        
        try {
            // Créer les paramètres de la session
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount((long) amountCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(credits + " Educational Credits")
                                                                    .setDescription("Achat de " + credits + " crédits pour accéder aux formations")
                                                                    .build()
                                                    )
                                                    .build()
                    )
                                    .build()
                    )
                    .setClientReferenceId(String.valueOf(userId))
                    .putMetadata("user_id", String.valueOf(userId))
                    .putMetadata("credits", String.valueOf(credits))
                    .putMetadata("email", user.getEmail())
                    .build();

            Session session = Session.create(params);

            // Enregistrer la transaction en base
            StripeTransaction transaction = new StripeTransaction(
                    userId,
                    session.getId(),
                    amountCents,
                    credits
            );
            transaction.setPaymentMethod("card");
            
            int transactionId = transactionDAO.createTransaction(transaction);
            
            logger.info("Session de checkout créée : sessionId=" + session.getId() + 
                       ", userId=" + userId + ", credits=" + credits);

            return new StripeCheckoutResponse(
                    session.getId(),
                    session.getUrl(),
                    transactionId,
                    user.getEmail(),
                    credits,
                    amountCents / 100.0
            );

        } catch (StripeException e) {
            logger.severe("Erreur Stripe lors de la création de la session : " + e.getMessage());
            throw new Exception("Erreur lors de la création de la session de paiement", e);
        }
    }

    /**
     * Récupère une transaction par son ID Stripe Session.
     */
    public StripeTransaction getTransactionBySessionId(String sessionId) {
        return transactionDAO.getTransactionByStripeSessionId(sessionId);
    }

    /**
     * Met à jour le solde du portefeuille après un paiement réussi.
     */
    public boolean completePayment(String sessionId, String paymentIntentId) throws Exception {
        
        StripeTransaction transaction = transactionDAO.getTransactionByStripeSessionId(sessionId);
        
        if (transaction == null) {
            logger.warning("Transaction non trouvée pour sessionId : " + sessionId);
            return false;
        }

        if (StripeTransaction.STATUS_COMPLETED.equals(transaction.getStatus())) {
            logger.info("Transaction déjà complétée : " + sessionId);
            return true;
        }

        // Mettre à jour le statut de la transaction
        transactionDAO.updateTransactionStatus(
                transaction.getId(),
                StripeTransaction.STATUS_COMPLETED,
                paymentIntentId
        );

        // Récupérer l'utilisateur
        User user = userDAO.trouverParId(transaction.getUserId());
        if (user == null) {
            logger.severe("Utilisateur non trouvé lors du complètement du paiement");
            return false;
        }

        // Mettre à jour le solde du portefeuille
        double creditsToAdd = transaction.getCredits();
        user.addCredits(creditsToAdd);
        
        boolean updated = userDAO.modifierUser(user);
        
        if (updated) {
            logger.info("Portefeuille mis à jour : userId=" + user.getId() + 
                       ", crédits ajoutés=" + creditsToAdd);
            return true;
        } else {
            logger.warning("Erreur lors de la mise à jour du portefeuille");
            return false;
        }
    }

    /**
     * Annule un paiement (ex: après webhook d'expiration).
     */
    public boolean cancelPayment(String sessionId) {
        
        StripeTransaction transaction = transactionDAO.getTransactionByStripeSessionId(sessionId);
        
        if (transaction == null) {
            logger.warning("Transaction non trouvée pour annulation : " + sessionId);
            return false;
        }

        return transactionDAO.updateTransactionStatus(
                transaction.getId(),
                StripeTransaction.STATUS_CANCELED,
                null
        );
    }

    /**
     * Récupère l'historique des transactions d'un utilisateur.
     */
    public List<StripeTransaction> getTransactionHistory(int userId) {
        return transactionDAO.getTransactionsByUserId(userId);
    }

    /**
     * Récupère le montant total dépensé par un utilisateur.
     */
    public double getTotalSpentByUser(int userId) {
        return transactionDAO.getTotalCompletedAmountForUser(userId);
    }

    /**
     * Obtient les options de crédits disponibles.
     */
    public Map<Integer, Integer> getAvailableCreditPackages() {
        return new HashMap<>(CREDIT_PRICING);
    }

    /**
     * Trouve le package de crédits minimum qui couvre le prix donné.
     * Par exemple: si le prix est 20$, retourne 25 crédits (24.99$)
     * @param priceUSD Le prix en dollars
     * @return Le nombre de crédits minimum qui couvre le prix
     */
    public int findMinimumCreditPackageForPrice(double priceUSD) {
        int priceInCents = (int) Math.ceil(priceUSD * 100);
        
        // Trier les packages par prix croissant et trouver le premier qui couvre le prix
        return CREDIT_PRICING.entrySet().stream()
                .filter(entry -> entry.getValue() >= priceInCents)
                .map(Map.Entry::getKey)
                .min(Integer::compare)
                .orElse(100); // Par défaut, retourner le package le plus élevé (100 crédits)
    }

    /**
     * Crée une clé publique sécurisée pour le client (sans clés sensibles).
     */
    public String getPublicStripeKey() {
        return configUtil.getStripePublicKey();
    }

    // ===================================================
    // Inner Class for Response
    // ===================================================

    public static class StripeCheckoutResponse {
        public final String sessionId;
        public final String checkoutUrl;
        public final int transactionId;
        public final String userEmail;
        public final int credits;
        public final double amountUSD;

        public StripeCheckoutResponse(String sessionId, String checkoutUrl, int transactionId,
                                    String userEmail, int credits, double amountUSD) {
            this.sessionId = sessionId;
            this.checkoutUrl = checkoutUrl;
            this.transactionId = transactionId;
            this.userEmail = userEmail;
            this.credits = credits;
            this.amountUSD = amountUSD;
        }

        @Override
        public String toString() {
            return "StripeCheckoutResponse{" +
                    "sessionId='" + sessionId + '\'' +
                    ", credits=" + credits +
                    ", amountUSD=" + amountUSD +
                    ", userEmail='" + userEmail + '\'' +
                    '}';
        }
    }
}
