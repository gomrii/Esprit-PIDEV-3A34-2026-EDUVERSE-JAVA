package Services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    public String traduire(String texte, String langueCible) {
        try {
            URL url = new URL("https://libretranslate.de/translate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // JSON propre (attention aux guillemets)
            String jsonInput = "{"
                    + "\"q\":\"" + escapeJson(texte) + "\","
                    + "\"source\":\"auto\","
                    + "\"target\":\"" + langueCible + "\","
                    + "\"format\":\"text\""
                    + "}";

            // envoyer requête
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();

            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String json = response.toString();

            // DEBUG utile (très important)
            System.out.println("Response API: " + json);

            // parsing sécurisé (évite IndexOutOfBounds)
            String marker = "\"translatedText\":\"";

            if (!json.contains(marker)) {
                System.err.println("Réponse invalide API !");
                return texte;
            }

            String result = json.split(marker)[1].split("\"")[0];

            return result;

        } catch (Exception e) {
            System.err.println("Erreur traduction : " + e.getMessage());
            return texte;
        }
    }

    // éviter les erreurs JSON avec " ou \
    private String escapeJson(String text) {
        return text.replace("\"", "\\\"");
    }
}