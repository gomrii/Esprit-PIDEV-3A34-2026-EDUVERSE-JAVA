package Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    public String traduire(String texte, String langueCible) {
        try {
            // Construction de l'URL API
            String urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl="
                    + langueCible + "&dt=t&q=" + URLEncoder.encode(texte, StandardCharsets.UTF_8);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Nettoyage du JSON pour extraire uniquement le texte
            String json = response.toString();
            return json.substring(4, json.indexOf("\","));

        } catch (Exception e) {
            System.err.println("Erreur traduction : " + e.getMessage());
            return texte; // Retourne le texte original si l'API échoue
        }
    }
}