package com.elearning.service;

import com.elearning.util.ApiConfigUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import java.util.logging.Logger;

/**
 * GroqAIService — Service pour générer des descriptions professionnelles avec Groq LLaMA AI.
 * 
 * Utilisation :
 * - Générer des descriptions pour formations (description professionnelle)
 * - Générer des bios pour utilisateurs
 * - Générer des résumés pour ressources
 * 
 * Modèle : llama-3.3-70b-versatile
 * Langue : Français professionnel
 * Temperature : 0.7 (créativité modérée)
 * Max tokens : 300
 */
public class GroqAIService {
    
    private static final Logger logger = Logger.getLogger(GroqAIService.class.getName());
    
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final float DEFAULT_TEMPERATURE = 0.7f;
    private static final int DEFAULT_MAX_TOKENS = 300;
    
    private final ApiConfigUtil configUtil;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public GroqAIService() {
        this.configUtil = ApiConfigUtil.getInstance();
        this.httpClient = HttpClientBuilder.create().build();
        this.apiKey = configUtil.getGroqApiKey();
        this.model = configUtil.getGroqModel();
        
        logger.info("GroqAIService initialisé avec modèle : " + this.model);
    }

    /**
     * Génère une description professionnelle en français pour une formation.
     * 
     * @param formationTitle Titre de la formation
     * @param courseLevel Niveau du cours (Débutant, Intermédiaire, Avancé)
     * @return Description générée ou null si erreur
     */
    public String generateFormationDescription(String formationTitle, String courseLevel) {
        
        String prompt = String.format(
            """
            Tu es un expert en création de contenu éducatif. 
            Génère une description professionnelle et attrayante pour cette formation en français.
            
            Titre: %s
            Niveau: %s
            
            Consignes :
            - Utilise un ton professionnel et engageant
            - Maximum 300 caractères
            - Inclus les bénéfices d'apprendre ce cours
            - Commence par un verbe d'action (Maîtrisez, Découvrez, Apprenez, etc.)
            - Pas de liste à puces, contenu fluide
            
            Description :
            """,
            formationTitle,
            courseLevel
        );

        return callGroqAPI(prompt, DEFAULT_TEMPERATURE, DEFAULT_MAX_TOKENS);
    }

    /**
     * Génère une biographie pour un utilisateur/instructor.
     * 
     * @param userName Nom de l'utilisateur
     * @param expertise Domaine d'expertise
     * @return Biographie générée
     */
    public String generateUserBiography(String userName, String expertise) {
        
        String prompt = String.format(
            """
            Génère une courte biographie professionnelle en français pour ce profil éducateur.
            
            Nom: %s
            Expertise: %s
            
            Consignes :
            - Ton professionnel et personnel
            - 2-3 phrases maximum
            - Mettez l'accent sur l'expérience et la passion
            - Pas de formules génériques
            
            Biographie :
            """,
            userName,
            expertise
        );

        return callGroqAPI(prompt, 0.6f, 150);
    }

    /**
     * Génère un résumé pour une ressource (vidéo, article, etc).
     * 
     * @param resourceTitle Titre de la ressource
     * @param resourceType Type (vidéo, article, PDF, etc)
     * @param context Contexte ou sujet
     * @return Résumé généré
     */
    public String generateResourceSummary(String resourceTitle, String resourceType, String context) {
        
        String prompt = String.format(
            """
            Génère un résumé court et informatif en français pour cette ressource éducative.
            
            Titre: %s
            Type: %s
            Contexte: %s
            
            Consignes :
            - Format concis et clair
            - Maximum 150 mots
            - Utilise des points clés
            - Adapte le ton au type de ressource
            
            Résumé :
            """,
            resourceTitle,
            resourceType,
            context
        );

        return callGroqAPI(prompt, DEFAULT_TEMPERATURE, 150);
    }

    /**
     * Génère un titre accrocheur pour une formation.
     * 
     * @param description Description courte du cours
     * @param targetAudience Audience cible
     * @return Titre généré
     */
    public String generateFormationTitle(String description, String targetAudience) {
        
        String prompt = String.format(
            """
            Génère un titre court et accrocheur en français pour cette formation.
            
            Description: %s
            Audience: %s
            
            Consignes :
            - Maximum 60 caractères
            - Utilise des mots impactants
            - Inclus des mots-clés pertinents
            - Rend-le irrésistible pour l'audience cible
            
            Titre :
            """,
            description,
            targetAudience
        );

        return callGroqAPI(prompt, 0.5f, 50);
    }

    /**
     * Appel générique à l'API Groq.
     * 
     * @param prompt Prompt pour le modèle
     * @param temperature Niveau de créativité (0.0 - 1.0)
     * @param maxTokens Nombre maximum de tokens
     * @return Réponse générée ou null si erreur
     */
    private String callGroqAPI(String prompt, float temperature, int maxTokens) {
        
        if (apiKey == null || apiKey.isBlank()) {
            logger.severe("Clé API Groq non configurée");
            return null;
        }

        try {
            // Construire la requête
            HttpPost request = new HttpPost(GROQ_API_URL);
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");

            // Créer le payload JSON
            JsonObject messages = new JsonObject();
            messages.addProperty("role", "user");
            messages.addProperty("content", prompt);

            JsonArray messagesArray = new JsonArray();
            messagesArray.add(messages);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.add("messages", messagesArray);
            requestBody.addProperty("temperature", temperature);
            requestBody.addProperty("max_tokens", maxTokens);

            request.setEntity(new StringEntity(requestBody.toString()));

            logger.info("Appel API Groq avec température=" + temperature + ", tokens=" + maxTokens);

            // Exécuter la requête
            return httpClient.execute(request, response -> {
                
                int statusCode = response.getCode();
                
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    
                    // Parser la réponse
                    JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                    
                    // Extraire le contenu généré
                    JsonArray choices = responseJson.getAsJsonArray("choices");
                    if (choices != null && choices.size() > 0) {
                        String generatedText = choices.get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content")
                            .getAsString()
                            .trim();
                        
                        logger.info("Contenu généré avec succès (longueur: " + generatedText.length() + ")");
                        return generatedText;
                    }
                    
                } else {
                    String errorBody = EntityUtils.toString(response.getEntity());
                    logger.severe("Erreur Groq API (code " + statusCode + "): " + errorBody);
                }
                
                return null;
            });

        } catch (Exception e) {
            logger.severe("Erreur lors de l'appel API Groq : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Vérifie que l'API Groq est accessible.
     */
    public boolean testConnection() {
        
        try {
            String testPrompt = "Réponds avec un seul mot : Groq";
            String response = callGroqAPI(testPrompt, 0.0f, 10);
            
            if (response != null && !response.isBlank()) {
                logger.info("Test de connexion Groq réussi ✓");
                return true;
            }
            
        } catch (Exception e) {
            logger.warning("Erreur lors du test de connexion Groq : " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Génère une description avec validation et gestion d'erreurs.
     * Retourne une description par défaut si l'API échoue.
     */
    public String generateDescriptionWithFallback(String title, String entityType) {
        
        try {
            String description = switch (entityType.toUpperCase()) {
                case "FORMATION" -> generateFormationDescription(title, "Général");
                case "RESSOURCE" -> generateResourceSummary(title, "Ressource", title);
                case "USER" -> generateUserBiography(title, "Éducateur");
                default -> null;
            };
            
            if (description != null && !description.isBlank()) {
                return description;
            }
            
        } catch (Exception e) {
            logger.warning("Erreur lors de la génération de description : " + e.getMessage());
        }
        
        // Fallback : description générique
        return getDefaultDescription(title, entityType);
    }

    /**
     * Retourne une description par défaut si l'API n'est pas disponible.
     */
    private String getDefaultDescription(String title, String entityType) {
        return switch (entityType.toUpperCase()) {
            case "FORMATION" -> "Découvrez " + title + " dans cette formation complète. Apprenez les concepts clés et développez vos compétences.";
            case "RESSOURCE" -> "Ressource : " + title + ". Explorez ce contenu pour approfondir vos connaissances.";
            case "USER" -> "Instructeur spécialisé en " + title + ". Passionné par l'éducation et le partage de connaissances.";
            default -> "Description non disponible pour : " + title;
        };
    }

    /**
     * Affiche les informations de configuration.
     */
    public void printConfigInfo() {
        logger.info("=== Groq AI Service Configuration ===");
        logger.info("Model: " + model);
        logger.info("Temperature: " + DEFAULT_TEMPERATURE);
        logger.info("Max Tokens: " + DEFAULT_MAX_TOKENS);
        logger.info("API Endpoint: " + GROQ_API_URL);
        logger.info("=====================================");
    }
}
