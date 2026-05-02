package Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {

    // REMPLACE PAR TA CLÉ (Celle de ton compte Gmail perso, pas @esprit.tn)
    private final String API_KEY = "";

    private final String API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:generateContent?key=" + API_KEY;
    public String genererContenuChapitre(String titreChapitre, String titreCours) {
        try {
            // Corps de la requête JSON
            String jsonBody = "{ \"contents\": [ { \"parts\": [ { \"text\": \"Rédige un texte sur le chapitre "
                    + titreChapitre + " du module " + titreCours + ". "
                    + "Le texte doit être en français simple et clair. "
                    + "Écris uniquement en paragraphes sans titres ni sections. "
                    + "Explique les concepts de manière facile pour un débutant. "
                    + "Ajoute quelques exemples simples si nécessaire. "
                    + "Utilise un style fluide et naturel. "
                    + "Le contenu doit être pédagogique. "
                    + "Ne fais pas de listes ni de numérotation. "
                    + "Évite les symboles comme ### ou ---. "
                    + "Génère exactement 6 phrases complètes.\" } ] } ] }";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);
                return jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject()
                        .getAsJsonObject("content").getAsJsonArray("parts")
                        .get(0).getAsJsonObject().get("text").getAsString();
            } else {
                System.err.println("Erreur Google : " + response.body());
                return "Erreur " + response.statusCode();
            }
        } catch (Exception e) {
            return "Exception : " + e.getMessage();
        }
    }
}