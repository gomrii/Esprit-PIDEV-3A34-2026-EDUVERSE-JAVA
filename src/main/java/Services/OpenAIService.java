package Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.*;

public class OpenAIService {
    private final String API_KEY = "TA_CLE_API";
    private final String URL = "https://api.openai.com/v1/chat/completions";
    private final Gson gson = new Gson();

    public String obtenirRaisonIA(String coursTitre, String cat, String hist) {
        try {
            String prompt = "L'étudiant a suivi : " + hist + ". Explique en une phrase courte pourquoi le cours '"
                    + coursTitre + "' (catégorie " + cat + ") est la suite logique.";

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);

            JsonObject body = new JsonObject();
            body.addProperty("model", "gpt-3.5-turbo");
            body.add("messages", gson.toJsonTree(new Object[]{message}));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
            return jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
        } catch (Exception e) {
            return "Ce cours correspond à votre profil d'apprentissage.";
        }
    }
}