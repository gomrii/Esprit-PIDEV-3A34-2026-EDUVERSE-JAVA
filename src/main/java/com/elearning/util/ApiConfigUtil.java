package com.elearning.util;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * ApiConfigUtil — Gère les clés API de manière sécurisée.
 * 
 * Priorité de configuration :
 * 1) Variables d'environnement système
 * 2) Fichier config/.env ou config/api.properties
 * 3) Valeurs par défaut (si disponibles)
 * 
 * IMPORTANT : Ne JAMAIS commiter les clés API dans Git !
 * Ajouter "config/.env" et "config/api.properties" à .gitignore
 */
public class ApiConfigUtil {
    
    private static final Logger logger = Logger.getLogger(ApiConfigUtil.class.getName());
    private static volatile ApiConfigUtil instance;
    private Properties config;

    // Stripe Configuration Keys
    public static final String STRIPE_SECRET_KEY = "STRIPE_SECRET_KEY";
    public static final String STRIPE_PUBLIC_KEY = "STRIPE_PUBLIC_KEY";
    public static final String STRIPE_WEBHOOK_SECRET = "STRIPE_WEBHOOK_SECRET";

    // Groq Configuration Keys
    public static final String GROQ_API_KEY = "GROQ_API_KEY";
    public static final String GROQ_MODEL = "GROQ_MODEL";

    // Webhook Configuration
    public static final String STRIPE_WEBHOOK_PORT = "STRIPE_WEBHOOK_PORT";
    public static final String STRIPE_WEBHOOK_PATH = "STRIPE_WEBHOOK_PATH";

    private ApiConfigUtil() {
        this.config = new Properties();
        loadConfiguration();
    }

    /**
     * Retourne l'instance singleton du gestionnaire de configuration API.
     */
    public static ApiConfigUtil getInstance() {
        if (instance == null) {
            synchronized (ApiConfigUtil.class) {
                if (instance == null) {
                    instance = new ApiConfigUtil();
                }
            }
        }
        return instance;
    }

    /**
     * Charge la configuration depuis plusieurs sources.
     */
    private void loadConfiguration() {
        try {
            // Essayer de charger depuis config/.env ou config/api.properties
            String[] configPaths = {
                "config/.env",
                "config/api.properties",
                System.getProperty("user.home") + "/.eduverse/api.properties"
            };

            for (String path : configPaths) {
                File file = new File(path);
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        config.load(fis);
                        logger.info("Configuration chargée depuis : " + path);
                        return;
                    }
                }
            }

            logger.warning("Aucun fichier de configuration trouvé. Utilisation des variables d'environnement.");
        } catch (IOException e) {
            logger.warning("Erreur lors du chargement de la configuration : " + e.getMessage());
        }
    }

    /**
     * Récupère une clé API avec priorité :
     * 1) Variables d'environnement
     * 2) Fichier properties
     * 3) Valeur par défaut
     */
    public String getConfigValue(String key, String defaultValue) {
        // 1) Vérifier les variables d'environnement (priorité la plus haute)
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        // 2) Vérifier les properties chargées
        String propValue = config.getProperty(key);
        if (propValue != null && !propValue.isBlank()) {
            return propValue;
        }

        // 3) Utiliser la valeur par défaut
        if (defaultValue != null) {
            return defaultValue;
        }

        throw new IllegalArgumentException(
            "Configuration manquante pour : " + key + 
            ". Veuillez définir une variable d'environnement ou ajouter au fichier de config."
        );
    }

    /**
     * Récupère une clé API sans valeur par défaut (obligatoire).
     */
    public String getConfigValue(String key) {
        return getConfigValue(key, null);
    }

    // ========================================
    // Stripe Configuration
    // ========================================

    public String getStripeSecretKey() {
        return getConfigValue(STRIPE_SECRET_KEY);
    }

    public String getStripePublicKey() {
        return getConfigValue(STRIPE_PUBLIC_KEY);
    }

    public String getStripeWebhookSecret() {
        return getConfigValue(STRIPE_WEBHOOK_SECRET);
    }

    public int getStripeWebhookPort() {
        try {
            return Integer.parseInt(getConfigValue(STRIPE_WEBHOOK_PORT, "8080"));
        } catch (NumberFormatException e) {
            logger.warning("Port webhook invalide, utilisation de 8080");
            return 8080;
        }
    }

    public String getStripeWebhookPath() {
        return getConfigValue(STRIPE_WEBHOOK_PATH, "/webhooks/stripe");
    }

    // ========================================
    // Groq Configuration
    // ========================================

    public String getGroqApiKey() {
        return getConfigValue(GROQ_API_KEY);
    }

    public String getGroqModel() {
        return getConfigValue(GROQ_MODEL, "llama-3.3-70b-versatile");
    }

    // ========================================
    // Validation
    // ========================================

    /**
     * Valide que toutes les clés essentielles sont disponibles.
     */
    public boolean validateConfiguration() {
        boolean valid = true;

        // Vérifier Stripe
        try {
            String secretKey = getStripeSecretKey();
            if (!secretKey.startsWith("sk_test_") && !secretKey.startsWith("sk_live_")) {
                logger.warning("Clé secrète Stripe invalide");
                valid = false;
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Clé secrète Stripe manquante");
            valid = false;
        }

        // Vérifier Groq
        try {
            String groqKey = getGroqApiKey();
            if (!groqKey.startsWith("gsk_")) {
                logger.warning("Clé API Groq invalide");
                valid = false;
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Clé API Groq manquante");
            valid = false;
        }

        return valid;
    }

    /**
     * Affiche le statut de la configuration (sans révéler les clés !).
     */
    public void logConfigurationStatus() {
        logger.info("=== Configuration API Status ===");
        
        try {
            String stripeSecret = getStripeSecretKey();
            logger.info("✓ Stripe Secret Key: " + (stripeSecret != null ? "configurée" : "manquante"));
        } catch (IllegalArgumentException e) {
            logger.info("✗ Stripe Secret Key: manquante");
        }

        try {
            String groqKey = getGroqApiKey();
            logger.info("✓ Groq API Key: " + (groqKey != null ? "configurée" : "manquante"));
        } catch (IllegalArgumentException e) {
            logger.info("✗ Groq API Key: manquante");
        }

        logger.info("=================================");
    }
}
