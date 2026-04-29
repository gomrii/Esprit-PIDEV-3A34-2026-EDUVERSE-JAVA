package com.elearning.service;

import com.elearning.dao.AIConversationDAO;
import com.elearning.dao.UserDAO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Assistant IA connecté à la BDD Eduverse — powered by Groq (Llama 3, GRATUIT).
 *
 * Groq offre 14 400 requêtes/jour gratuitement, sans carte bancaire.
 * Clé gratuite sur : https://console.groq.com/keys
 *
 * API compatible OpenAI : POST https://api.groq.com/openai/v1/chat/completions
 */
public class AIAssistantService {

    // ✅ Groq — 100% GRATUIT, ultra-rapide (Llama 3.1)
    private static final String API_KEY = "VOTRE_CLE_GROQ_ICI";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.1-8b-instant";
    private static final int    TIMEOUT_S = 20;

    private final UserDAO           userDAO  = new UserDAO();
    private final AIConversationDAO convDAO  = new AIConversationDAO();
    private final Gson              gson     = new Gson();

    public String poserQuestion(String question, int adminId) throws Exception {

        // Données BDD en temps réel
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("total",       userDAO.compterTotal());
        ctx.put("etudiants",   userDAO.compterParRole("ETUDIANT"));
        ctx.put("enseignants", userDAO.compterParRole("ENSEIGNANT"));
        ctx.put("admins",      userDAO.compterParRole("ADMIN"));
        ctx.put("actifs",      userDAO.compterParStatut("ACTIF"));
        ctx.put("bloques",     userDAO.compterParStatut("BLOQUE"));
        ctx.put("en_attente",  userDAO.compterParStatut("EN_ATTENTE"));

        List<String> enAttente = userDAO.rechercherUsers("", "", "created_at", "DESC")
                .stream()
                .filter(u -> "EN_ATTENTE".equals(u.getStatut()))
                .limit(5)
                .map(u -> u.getFullName() + " <" + u.getEmail() + ">")
                .collect(Collectors.toList());
        ctx.put("derniers_en_attente", enAttente);
        ctx.put("date", java.time.LocalDate.now().toString());

        String systemPrompt = """
                Tu es l'assistant IA de la plateforme e-learning Eduverse.
                Tu aides l'administrateur à gérer les utilisateurs.
                Réponds TOUJOURS en français, de façon concise et utile (max 150 mots).
                N'invente aucune donnée. Utilise uniquement les données ci-dessous.

                === DONNÉES RÉELLES BDD (temps réel) ===
                Total utilisateurs  : %d
                Étudiants           : %d
                Enseignants         : %d
                Administrateurs     : %d
                Comptes actifs      : %d
                Comptes bloqués     : %d
                Comptes en attente  : %d
                En attente (détail) : %s
                Date du jour        : %s
                ========================================
                """.formatted(
                (int) ctx.get("total"), (int) ctx.get("etudiants"),
                (int) ctx.get("enseignants"), (int) ctx.get("admins"),
                (int) ctx.get("actifs"), (int) ctx.get("bloques"),
                (int) ctx.get("en_attente"), ctx.get("derniers_en_attente"),
                ctx.get("date")
        );

        String reponse = appellerGroq(systemPrompt, question);

        try { convDAO.sauvegarder(adminId, question, reponse, gson.toJson(ctx)); }
        catch (Exception e) { System.err.println("⚠ Conv save: " + e.getMessage()); }

        return reponse;
    }

    private String appellerGroq(String systemPrompt, String question) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_S, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_S, TimeUnit.SECONDS)
                .build();

        // Format OpenAI-compatible
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", question);

        JsonArray messages = new JsonArray();
        messages.add(sysMsg);
        messages.add(userMsg);

        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.add("messages", messages);
        body.addProperty("max_tokens", 512);
        body.addProperty("temperature", 0.7);

        RequestBody requestBody = RequestBody.create(
                gson.toJson(body), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println("📡 Appel Groq API (" + MODEL + ")...");

        try (Response response = client.newCall(request).execute()) {
            String bodyStr = response.body() != null ? response.body().string() : "";
            System.out.println("📥 Groq (" + response.code() + "): " + bodyStr.substring(0, Math.min(120, bodyStr.length())));

            if (!response.isSuccessful()) {
                throw new Exception("Erreur API Groq (" + response.code() + ") : " + bodyStr);
            }

            return gson.fromJson(bodyStr, JsonObject.class)
                       .getAsJsonArray("choices").get(0).getAsJsonObject()
                       .getAsJsonObject("message")
                       .get("content").getAsString().trim();
        }
    }
}
