package com.elearning.service;

import com.elearning.entity.StripeTransaction;
import com.elearning.util.ApiConfigUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * StripeWebhookHandler — Traite les événements Stripe via webhooks.
 * 
 * Événements gérés :
 * - checkout.session.completed : Paiement réussi
 * - checkout.session.expired : Session expirée
 * - charge.failed : Paiement échoué
 * 
 * IMPORTANT : Les webhooks doivent être sécurisés avec une signature Stripe !
 */
public class StripeWebhookHandler {
    
    private static final Logger logger = Logger.getLogger(StripeWebhookHandler.class.getName());
    
    private final StripePaymentService paymentService;
    private final ApiConfigUtil configUtil;

    public StripeWebhookHandler() {
        this.paymentService = new StripePaymentService();
        this.configUtil = ApiConfigUtil.getInstance();
    }

    /**
     * Traite un événement webhook Stripe.
     * 
     * @param payload Contenu brut du webhook
     * @param signature Signature Stripe pour validation
     * @return true si l'événement a été traité avec succès
     */
    public boolean handleWebhookEvent(String payload, String signature) {
        
        if (payload == null || signature == null) {
            logger.warning("Payload ou signature manquant");
            return false;
        }

        try {
            // Vérifier la signature du webhook
            String webhookSecret = configUtil.getStripeWebhookSecret();
            Event event = verifyAndParseEvent(payload, signature, webhookSecret);

            if (event == null) {
                logger.warning("Impossible de vérifier l'événement webhook");
                return false;
            }

            logger.info("Événement webhook reçu : " + event.getType());

            // Router selon le type d'événement
            switch (event.getType()) {
                case "checkout.session.completed":
                    return handleCheckoutSessionCompleted(event);
                    
                case "checkout.session.expired":
                    return handleCheckoutSessionExpired(event);
                    
                case "charge.failed":
                    return handleChargeFailed(event);
                    
                default:
                    logger.info("Événement non géré : " + event.getType());
                    return true; // Reconnaître mais ignorer
            }

        } catch (Exception e) {
            logger.severe("Erreur lors du traitement du webhook : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Traite un événement checkout.session.completed.
     * Appelé quand un paiement est validé avec succès.
     */
    private boolean handleCheckoutSessionCompleted(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject()
                    .orElse(null);

            if (session == null) {
                logger.warning("Session Stripe non trouvée dans l'événement");
                return false;
            }

            logger.info("Paiement complété : sessionId=" + session.getId() + 
                       ", paymentIntentId=" + session.getPaymentIntent());

            // Mettre à jour le paiement
            boolean success = paymentService.completePayment(
                    session.getId(),
                    session.getPaymentIntent()
            );

            if (success) {
                logger.info("Paiement traité avec succès pour session : " + session.getId());
            } else {
                logger.warning("Erreur lors du traitement du paiement");
            }

            return success;

        } catch (Exception e) {
            logger.severe("Erreur lors du traitement de checkout.session.completed : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Traite un événement checkout.session.expired.
     * Appelé quand une session de checkout expire sans paiement.
     */
    private boolean handleCheckoutSessionExpired(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject()
                    .orElse(null);

            if (session == null) {
                logger.warning("Session Stripe non trouvée");
                return false;
            }

            logger.info("Session expirée : " + session.getId());

            // Marquer la transaction comme annulée
            boolean success = paymentService.cancelPayment(session.getId());

            if (success) {
                logger.info("Transaction annulée (expiration) : " + session.getId());
            }

            return success;

        } catch (Exception e) {
            logger.severe("Erreur lors du traitement de checkout.session.expired : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Traite un événement charge.failed.
     * Appelé quand un paiement échoue.
     */
    private boolean handleChargeFailed(Event event) {
        try {
            logger.info("Paiement échoué - Événement reçu");
            logger.warning("Charge failed for event: " + event.getId());
            
            // Vous pouvez ajouter de la logique personnalisée ici
            // pour notifier l'utilisateur, enregistrer les détails de l'erreur, etc.

            return true;

        } catch (Exception e) {
            logger.severe("Erreur lors du traitement de charge.failed : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie la signature Stripe et analyse l'événement.
     * 
     * @param payload Contenu brut du webhook
     * @param signature Signature HTTP Stripe (en-tête Stripe-Signature)
     * @param webhookSecret Secret du webhook Stripe
     * @return Événement Stripe parsé et validé, ou null si invalide
     */
    private Event verifyAndParseEvent(String payload, String signature, String webhookSecret) {
        try {
            // Vérifier la signature Stripe
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);

            logger.info("Signature Stripe vérifiée avec succès");
            return event;

        } catch (SignatureVerificationException e) {
            logger.severe("ERREUR CRITIQUE : Signature Stripe invalide ! " +
                         "Possible tentative d'usurpation. " + e.getMessage());
            return null;

        } catch (Exception e) {
            logger.severe("Erreur lors de la vérification du webhook : " + e.getMessage());
            return null;
        }
    }

    /**
     * Teste la configuration du webhook.
     * Vérifie que le secret est disponible.
     */
    public boolean testWebhookConfiguration() {
        try {
            String secret = configUtil.getStripeWebhookSecret();
            if (secret != null && !secret.isBlank()) {
                logger.info("Configuration webhook Stripe valide");
                return true;
            }
            logger.warning("Secret webhook Stripe manquant");
            return false;
        } catch (Exception e) {
            logger.warning("Erreur lors de la vérification de la config webhook : " + e.getMessage());
            return false;
        }
    }

    
    public static void printWebhookIntegrationExample() {
        String example = """
            
            ========== Exemple d'intégration Webhook ==========
            
            Pour une application JavaFX, créer un serveur HTTP simple :
            
            import com.sun.net.httpserver.*;
            import java.io.*;
            import java.net.InetSocketAddress;
            
            public class StripeWebhookServer {
                public static void startWebhookServer(int port) throws IOException {
                    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
                    
                    server.createContext("/webhooks/stripe", exchange -> {
                        if ("POST".equals(exchange.getRequestMethod())) {
                            // Lire le payload
                            BufferedReader reader = new BufferedReader(
                                new InputStreamReader(exchange.getRequestBody())
                            );
                            StringBuilder payload = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                payload.append(line);
                            }
                            
                            // Récupérer la signature
                            String signature = exchange.getHeaders()
                                .getFirst("Stripe-Signature");
                            
                            // Traiter l'événement
                            StripeWebhookHandler handler = new StripeWebhookHandler();
                            boolean success = handler.handleWebhookEvent(
                                payload.toString(), 
                                signature
                            );
                            
                            // Retourner 200 OK si succès
                            exchange.sendResponseHeaders(success ? 200 : 400, 0);
                            exchange.close();
                        }
                    });
                    
                    server.start();
                    System.out.println("Webhook server démarré sur le port " + port);
                }
            }
            
            ====================================================
            """;
        
        System.out.println(example);
    }
}
