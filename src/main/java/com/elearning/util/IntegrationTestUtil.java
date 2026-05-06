package com.elearning.util;

import com.elearning.service.GroqAIService;
import com.elearning.service.StripePaymentService;
import java.util.Map;
import java.util.logging.Logger;

/**
 * IntegrationTestUtil — Classe pour tester les intégrations API.
 * 
 * Utilisation :
 * java -cp . com.elearning.util.IntegrationTestUtil
 * 
 * Ou dans le code :
 * IntegrationTestUtil.runAllTests();
 */
public class IntegrationTestUtil {
    
    private static final Logger logger = Logger.getLogger(IntegrationTestUtil.class.getName());

    /**
     * Lance tous les tests d'intégration.
     */
    public static void runAllTests() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🧪 TESTS D'INTÉGRATION API");
        System.out.println("=".repeat(60) + "\n");

        testConfigUtilConfiguration();
        testStripeConfiguration();
        testGroqConfiguration();
        testCreditPricing();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✓ Tests terminés");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Test 1 : Configuration générale
     */
    private static void testConfigUtilConfiguration() {
        System.out.println("\n📋 Test 1 : Configuration ApiConfigUtil");
        System.out.println("-".repeat(40));
        
        try {
            ApiConfigUtil configUtil = ApiConfigUtil.getInstance();
            
            System.out.println("Vérification de la configuration...");
            configUtil.logConfigurationStatus();
            
            if (configUtil.validateConfiguration()) {
                System.out.println("✓ Configuration valide");
            } else {
                System.out.println("✗ Configuration incomplète");
            }
            
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    /**
     * Test 2 : Configuration Stripe
     */
    private static void testStripeConfiguration() {
        System.out.println("\n💳 Test 2 : Configuration Stripe");
        System.out.println("-".repeat(40));
        
        try {
            ApiConfigUtil configUtil = ApiConfigUtil.getInstance();
            
            String secretKey = configUtil.getStripeSecretKey();
            String publicKey = configUtil.getStripePublicKey();
            String webhookSecret = configUtil.getStripeWebhookSecret();
            
            System.out.println("Secret Key présente : " + (secretKey != null ? "✓" : "✗"));
            System.out.println("Public Key présente : " + (publicKey != null ? "✓" : "✗"));
            System.out.println("Webhook Secret présent : " + (webhookSecret != null ? "✓" : "✗"));
            
            if (secretKey != null && secretKey.startsWith("sk_")) {
                System.out.println("Format Secret Key : ✓ (sk_...)");
            } else {
                System.out.println("Format Secret Key : ✗");
            }
            
            if (publicKey != null && publicKey.startsWith("pk_")) {
                System.out.println("Format Public Key : ✓ (pk_...)");
            } else {
                System.out.println("Format Public Key : ✗");
            }
            
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    /**
     * Test 3 : Configuration Groq
     */
    private static void testGroqConfiguration() {
        System.out.println("\n🤖 Test 3 : Configuration Groq AI");
        System.out.println("-".repeat(40));
        
        try {
            ApiConfigUtil configUtil = ApiConfigUtil.getInstance();
            String apiKey = configUtil.getGroqApiKey();
            String model = configUtil.getGroqModel();
            
            System.out.println("API Key présente : " + (apiKey != null ? "✓" : "✗"));
            System.out.println("Modèle configuré : " + model);
            
            if (apiKey != null && apiKey.startsWith("gsk_")) {
                System.out.println("Format API Key : ✓ (gsk_...)");
            } else {
                System.out.println("Format API Key : ✗");
            }
            
            System.out.println("\nTest de connexion à Groq API...");
            GroqAIService groqService = new GroqAIService();
            
            if (groqService.testConnection()) {
                System.out.println("Connexion Groq : ✓ OK");
            } else {
                System.out.println("Connexion Groq : ✗ ÉCHOUÉE");
            }
            
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    /**
     * Test 4 : Pricing des crédits Stripe
     */
    private static void testCreditPricing() {
        System.out.println("\n💰 Test 4 : Pricing des crédits");
        System.out.println("-".repeat(40));
        
        try {
            Map<Integer, Integer> pricing = StripePaymentService.CREDIT_PRICING;
            
            System.out.println("Packages disponibles :");
            
            for (Integer credits : pricing.keySet()) {
                int cents = pricing.get(credits);
                double dollars = cents / 100.0;
                double pricePerCredit = dollars / credits;
                
                System.out.printf("  • %3d crédits = $%6.2f (%.2f¢ par crédit)%n", 
                    credits, dollars, pricePerCredit);
            }
            
        } catch (Exception e) {
            System.out.println("✗ Erreur : " + e.getMessage());
        }
    }

    /**
     * Affiche un guide d'utilisation rapide.
     */
    public static void printQuickStart() {
        String guide = """
            
            ╔════════════════════════════════════════════════════════════════════════════╗
            ║                     GUIDE RAPIDE - Intégration API                         ║
            ╚════════════════════════════════════════════════════════════════════════════╝
            
            📌 ÉTAPE 1 : Configuration
            ──────────────────────────
            1. Créer le fichier : config/api.properties
            2. Ajouter les clés API (voir exemple ci-dessous)
            3. Ajouter à .gitignore : config/api.properties
            
            
            📌 ÉTAPE 2 : Initialiser les services
            ──────────────────────────────────────
            // Stripe Payment
            StripePaymentService stripeService = new StripePaymentService();
            
            // Groq AI
            GroqAIService groqService = new GroqAIService();
            
            
            📌 ÉTAPE 3 : Utiliser les services
            ───────────────────────────────────
            
            // Créer une session de paiement Stripe
            StripePaymentService.StripeCheckoutResponse response = 
                stripeService.createCheckoutSession(
                    userId,      // ID utilisateur
                    25,          // Nombre de crédits
                    successUrl,  // URL de retour succès
                    cancelUrl    // URL de retour annulation
                );
            
            // Générer une description avec Groq AI
            String description = groqService.generateFormationDescription(
                "Ma Formation",
                "Débutant"
            );
            
            
            📌 ÉTAPE 4 : Intégrer dans les contrôleurs
            ────────────────────────────────────────
            - Voir API_INTEGRATION_GUIDE.md pour les exemples complets
            - Voir les classes CreditPurchaseController et FormationFormController
            
            
            📌 ÉTAPE 5 : Déployer en production
            ───────────────────────────────────
            1. Passer les clés en mode LIVE (sk_live_... et pk_live_...)
            2. Configurer les webhooks Stripe
            3. Tester entièrement en mode production
            4. Monitorer les transactions
            
            
            🔑 FORMAT DES CLÉS API
            ─────────────────────
            
            Stripe (Mode Test) :
              Secret Key  : sk_test_... (70+ caractères)
              Public Key  : pk_test_... (70+ caractères)
              Webhook Secret : whsec_... (50+ caractères)
            
            Groq AI :
              API Key     : gsk_....... (70+ caractères)
            
            
            ⚠️  SÉCURITÉ CRITIQUE
            ────────────────────
            ✗ NE JAMAIS commiter les clés API dans Git
            ✗ NE JAMAIS hardcoder les clés dans le code
            ✗ NE JAMAIS partager les clés sur les réseaux
            ✓ Utiliser des variables d'environnement
            ✓ Utiliser un fichier .env local (gitignored)
            ✓ Faire tourner les webhooks en HTTPS
            
            
            📝 FICHIERS IMPORTANTS
            ──────────────────────
            - API_INTEGRATION_GUIDE.md : Documentation complète
            - config/api.properties.example : Exemple de configuration
            - src/.../service/StripePaymentService.java : Service de paiement
            - src/.../service/GroqAIService.java : Service d'IA
            - src/.../util/ApiConfigUtil.java : Gestionnaire de config
            
            
            🧪 TESTS
            ────────
            // Lancer les tests d'intégration
            IntegrationTestUtil.runAllTests();
            
            // Ou via command line
            java com.elearning.util.IntegrationTestUtil
            
            ═══════════════════════════════════════════════════════════════════════════════
            """;
        
        System.out.println(guide);
    }

    /**
     * Point d'entrée pour exécuter les tests.
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--help")) {
            printQuickStart();
        } else {
            runAllTests();
            System.out.println("\nPour plus de détails, exécutez :");
            System.out.println("  java com.elearning.util.IntegrationTestUtil --help");
        }
    }
}
