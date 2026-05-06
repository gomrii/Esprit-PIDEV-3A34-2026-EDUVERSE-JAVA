package Services;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;

public class TTSService {

    // Remplace par ta clé VoiceRSS
    private static final String API_KEY = "2277e596aafc43ccb8198d317407ea30";

        public MediaPlayer lireTexte(String texte) {
            try {
                // Encodage du texte pour l'URL
                String urlString = "http://api.voicerss.org/?key=" + API_KEY +
                        "&hl=fr-fr&c=MP3&src=" + URLEncoder.encode(texte, "UTF-8");

                // Fichier temporaire unique pour éviter les conflits de lecture
                File tempFile = new File("audio_temp.mp3");
                URL url = new URL(urlString);

                try (InputStream in = url.openStream();
                     OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                // On crée le média et le player
                Media hit = new Media(tempFile.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(hit);

                // On retourne l'objet sans faire .play() ici
                return mediaPlayer;

            } catch (Exception e) {
                System.err.println("Erreur API VoiceRSS : " + e.getMessage());
                return null;
            }
        }
    }
