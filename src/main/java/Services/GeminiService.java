package Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


public class GeminiService {

    private static final String API_KEY = "VOTRE_CLE_GROQ_ICI";
    
    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";

    public static String generateDescription(String title, String date, String location) throws Exception {
        String prompt = String.format(
            "Generate a professional and engaging event description based ONLY on the information provided below:\n\n" +
            "Title: %s\n" +
            "Date: %s\n" +
            "Location: %s\n\n" +
            "Rules:\n" +
            "- Write in French\n" +
            "- 3 to 4 lines only\n" +
            "- Make it attractive and clear for students or general audience\n" +
            "- Do not invent extra information\n" +
            "- Keep it natural, professional, and easy to read\n\n" +
            "Return ONLY the description text.",
            title, date, location
        );
        
        // Escape prompt for JSON
        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");

        String jsonRequest = "{" +
                "\"model\": \"llama-3.3-70b-versatile\"," +
                "\"messages\": [" +
                "  {\"role\": \"system\", \"content\": \"You are an AI event description generator.\"}," +
                "  {\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}" +
                "]," +
                "\"temperature\": 0.7" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            return "ERREUR API GROQ (" + response.statusCode() + ") :\n" + response.body();
        }

        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray choices = jsonObject.getAsJsonArray("choices");
        
        if (choices != null && choices.size() > 0) {
            return choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString().trim();
        }
        
        return "Désolé, impossible de générer la description.";
    }
}
