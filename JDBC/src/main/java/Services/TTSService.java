package Services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;

public class TTSService {

    // Remplace par ta clé VoiceRSS
    private static final String API_KEY = "TON_API_KEY_ICI";

    public void lireTexte(String texte) {
        try {
            // Configuration de l'URL de l'API (Langue: fr-fr, Format: mp3, Vitesse: normale)
            String urlString = "http://api.voicerss.org/?key=" + API_KEY +
                    "&hl=fr-fr&c=MP3&src=" + URLEncoder.encode(texte, "UTF-8");

            // On crée un fichier temporaire pour stocker l'audio
            File tempFile = new File("audio_cours.mp3");
            URL url = new URL(urlString);

            try (InputStream in = url.openStream();
                 OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Lecture avec le MediaPlayer de JavaFX
            Media hit = new Media(tempFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();

        } catch (Exception e) {
            System.err.println("Erreur API VoiceRSS : " + e.getMessage());
        }
    }
}