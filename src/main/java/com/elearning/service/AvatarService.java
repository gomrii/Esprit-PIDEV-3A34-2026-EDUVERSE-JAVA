package com.elearning.service;

import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Intègre l'API DiceBear pour générer des avatars PNG uniques par utilisateur.
 * API gratuite, sans clé d'API requise.
 *
 * URL : https://api.dicebear.com/7.x/avataaars/png?seed={nom}
 *
 * IMPORTANT : Toujours appeler genererAvatar() dans un Task JavaFX.
 */
public class AvatarService {

    // PNG au lieu de SVG — JavaFX supporte nativement PNG
    private static final String API_BASE    = "https://api.dicebear.com/7.x/avataaars/png";
    private static final String AVATARS_DIR = "avatars/";  // dossier à la racine du projet
    private static final int    TIMEOUT_S   = 10;

    // -------------------------------------------------------
    // Génération de l'avatar
    // -------------------------------------------------------

    /**
     * Appelle l'API DiceBear, récupère le PNG et le sauvegarde localement.
     *
     * @param seed   graine pour la génération (ex: nom de l'utilisateur)
     * @param userId ID de l'utilisateur (pour le nom de fichier)
     * @return chemin absolu du fichier PNG sauvegardé localement
     * @throws Exception si l'API est inaccessible
     */
    public String genererAvatar(String seed, int userId) throws Exception {
        // Créer le dossier avatars si inexistant
        Path dir = Paths.get(AVATARS_DIR);
        Files.createDirectories(dir);

        // Construire l'URL DiceBear (PNG)
        String url = construireUrl(seed);
        System.out.println("📡 Appel DiceBear : " + url);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_S, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_S, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "EduverseApp/1.0")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API DiceBear : réponse HTTP " + response.code());
            }
            byte[] pngBytes = response.body().bytes();
            if (pngBytes.length < 100) {
                throw new IOException("Réponse DiceBear vide ou invalide");
            }

            // Nom du fichier PNG
            String safeSeed = (seed != null ? seed : "user").replaceAll("[^a-zA-Z0-9]", "_");
            String fileName = (userId > 0)
                    ? "avatar_" + userId + ".png"
                    : "avatar_" + safeSeed + ".png";

            Path dest = dir.resolve(fileName);
            Files.write(dest, pngBytes);

            String cheminAbsolu = dest.toAbsolutePath().toString();
            System.out.println("✅ Avatar PNG généré : " + cheminAbsolu);
            return cheminAbsolu;
        }
    }

    // -------------------------------------------------------
    // Chargement d'une image JavaFX
    // -------------------------------------------------------

    /**
     * Charge une image JavaFX depuis un chemin local (PNG/JPG).
     * Retourne l'image par défaut si introuvable.
     */
    public javafx.scene.image.Image chargerImage(String picturePath) {
        if (picturePath == null || picturePath.isBlank()) {
            return chargerImageParDefaut();
        }
        try {
            java.io.File f = new java.io.File(picturePath);
            if (f.exists()) {
                String uri = f.toURI().toString();
                System.out.println("🖼 Chargement image : " + uri);
                return new javafx.scene.image.Image(uri);
            }
            System.err.println("⚠ Fichier introuvable : " + picturePath);
            return chargerImageParDefaut();
        } catch (Exception e) {
            System.err.println("⚠ Impossible de charger l'image : " + e.getMessage());
            return chargerImageParDefaut();
        }
    }

    /** Retourne une image par défaut (logo Eduverse). */
    private javafx.scene.image.Image chargerImageParDefaut() {
        try {
            var stream = getClass().getResourceAsStream("/com/elearning/images/logo.png");
            if (stream != null) return new javafx.scene.image.Image(stream);
            // Si pas de logo, retourner null (l'ImageView restera vide)
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------------------------------------
    // Construction de l'URL DiceBear PNG
    // -------------------------------------------------------

    private String construireUrl(String seed) {
        String encodedSeed = URLEncoder.encode(
                seed != null ? seed : "user", StandardCharsets.UTF_8);
        return API_BASE
             + "?seed="            + encodedSeed
             + "&backgroundColor=" + "b6e3f4"
             + "&radius=50"
             + "&size=128";
    }
}
