package com.elearning.service;

import com.elearning.dao.UserDAO;
import com.elearning.dao.AIRecommendationDAO;
import com.elearning.entity.AIRecommendation;
import com.elearning.entity.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AIRecommendationService {

    private static final String API_KEY   = "VOTRE_CLE_GROQ_ICI";
    private static final String API_URL   = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL     = "llama-3.1-8b-instant";
    private static final int    TIMEOUT_S = 20;

    private final UserDAO userDAO = new UserDAO();
    private final AIRecommendationDAO recDAO = new AIRecommendationDAO();
    private final Gson gson = new Gson();

    public List<AIRecommendation> analyserEtRecommander() throws Exception {
        Map<String, Object> donnees = collecterDonnees();
        String prompt = construirePromptAnalyse(donnees);
        String reponseJson = appellerIA(prompt);
        List<AIRecommendation> recommandations = parserRecommandations(reponseJson);
        
        // Supprimer les anciennes pour ne pas surcharger
        recDAO.supprimerAnciennesRecommandations();
        recDAO.sauvegarderRecommandations(recommandations);

        return recommandations;
    }

    private Map<String, Object> collecterDonnees() {
        Map<String, Object> d = new LinkedHashMap<>();

        List<User> enAttenteAncienes = userDAO.rechercherUsers("", "", "created_at", "ASC")
            .stream()
            .filter(u -> "EN_ATTENTE".equals(u.getStatut()))
            .filter(u -> u.getCreatedAt() != null &&
                u.getCreatedAt().isBefore(LocalDateTime.now().minusDays(3)))
            .limit(10)
            .toList();
            
        d.put("en_attente_anciens", enAttenteAncienes.stream()
            .map(u -> u.getFullName() + " (depuis " +
                u.getCreatedAt().toLocalDate() + ")")
            .toList());

        List<User> bloques = userDAO.rechercherUsers("", "", "created_at", "DESC")
            .stream().filter(User::isBlocked).limit(5).toList();
        d.put("bloques", bloques.stream().map(User::getFullName).toList());

        d.put("total", userDAO.compterTotal());
        d.put("en_attente", userDAO.compterParStatut("EN_ATTENTE"));
        d.put("bloques_count", userDAO.compterParStatut("BLOQUE"));
        d.put("date", LocalDate.now().toString());

        return d;
    }

    private String construirePromptAnalyse(Map<String, Object> donnees) {
        return """
            Tu es un système d'analyse IA pour la plateforme Eduverse.
            Analyse ces données et génère des recommandations d'actions CONCRÈTES pour l'administrateur.

            RÉPONDS UNIQUEMENT EN JSON avec ce format exact :
            {
              "recommandations": [
                {
                  "priorite": "HAUTE",
                  "type": "APPROBATION",
                  "titre": "Titre court de l'action",
                  "description": "Description détaillée en français (max 50 mots)",
                  "action_suggeree": "Action précise à faire"
                }
              ]
            }
            La priorité peut être HAUTE, MOYENNE ou FAIBLE.
            Le type peut être APPROBATION, BLOCAGE, DEBLOCAGE, ALERTE ou INFO.

            === DONNÉES ACTUELLES DE LA BDD ===
            Total utilisateurs : %d
            En attente d'approbation : %d
            Comptes bloqués : %d
            En attente depuis +3 jours : %s
            Bloqués : %s
            Date : %s
            =================================

            Génère entre 2 et 5 recommandations pertinentes uniquement.
            Ne génère PAS de recommandations inutiles ou évidentes.
            """.formatted(
                (int) donnees.get("total"),
                (int) donnees.get("en_attente"),
                (int) donnees.get("bloques_count"),
                donnees.get("en_attente_anciens").toString(),
                donnees.get("bloques").toString(),
                donnees.get("date")
            );
    }

    private String appellerIA(String prompt) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_S, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_S, TimeUnit.SECONDS)
                .build();

        JsonArray messages = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);
        messages.add(userMsg);

        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.add("messages", messages);
        body.addProperty("temperature", 0.3); // Low temp for JSON reliability

        RequestBody reqBody = RequestBody.create(
                body.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(reqBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new Exception("API erreur: " + response.code());
            }

            String jsonResponse = response.body().string();
            JsonObject jsonObj = gson.fromJson(jsonResponse, JsonObject.class);
            return jsonObj.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }

    private List<AIRecommendation> parserRecommandations(String json) {
        List<AIRecommendation> liste = new ArrayList<>();
        try {
            // Find the start of JSON (to handle potential markdown blocks from LLM)
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }

            JsonObject root = gson.fromJson(json, JsonObject.class);
            if (root.has("recommandations")) {
                JsonArray arr = root.getAsJsonArray("recommandations");
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();
                    AIRecommendation rec = new AIRecommendation();
                    rec.setPriorite(obj.has("priorite") ? obj.get("priorite").getAsString() : "FAIBLE");
                    rec.setType(obj.has("type") ? obj.get("type").getAsString() : "INFO");
                    rec.setTitre(obj.has("titre") ? obj.get("titre").getAsString() : "Nouvelle recommandation");
                    rec.setDescription(obj.has("description") ? obj.get("description").getAsString() : "");
                    rec.setActionSuggeree(obj.has("action_suggeree") ? obj.get("action_suggeree").getAsString() : "");
                    rec.setLue(false);
                    liste.add(rec);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing JSON Recommandations: " + e.getMessage());
        }
        return liste;
    }
}
