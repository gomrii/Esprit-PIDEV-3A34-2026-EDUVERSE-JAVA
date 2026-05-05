package com.elearning.dao;

import com.elearning.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO pour la table `password_reset_token`.
 * Gère les tokens de réinitialisation de mot de passe.
 */
public class PasswordResetDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // -------------------------------------------------------
    // Créer un token
    // -------------------------------------------------------

    /**
     * Sauvegarde un token de réinitialisation en BDD.
     *
     * @param userId     ID de l'utilisateur
     * @param token      token hex 32 caractères
     * @param expiration date/heure d'expiration
     */
    public void sauvegarderToken(int userId, String token, LocalDateTime expiration) {
        // Supprimer les anciens tokens non utilisés avant d'en créer un nouveau
        supprimerAncienTokens(userId);

        String sql = "INSERT INTO password_reset_token (user_id, token, expires_at, used, created_at) "
                   + "VALUES (?, ?, ?, 0, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(3, Timestamp.valueOf(expiration));
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur PasswordResetDAO.sauvegarderToken : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Valider un token
    // -------------------------------------------------------

    /**
     * Vérifie si le token est valide, non expiré et non utilisé.
     *
     * @param token le token à vérifier
     * @return l'ID de l'utilisateur si valide, -1 sinon
     */
    public int validerToken(String token) {
        String sql = "SELECT user_id, expires_at, used FROM password_reset_token "
                   + "WHERE token = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean used      = rs.getBoolean("used");
                    Timestamp expires = rs.getTimestamp("expires_at");
                    if (!used && expires != null
                            && expires.toLocalDateTime().isAfter(LocalDateTime.now())) {
                        return rs.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur PasswordResetDAO.validerToken : " + e.getMessage());
        }
        return -1;
    }

    // -------------------------------------------------------
    // Consommer un token (usage unique)
    // -------------------------------------------------------

    /**
     * Marque le token comme utilisé après une réinitialisation réussie.
     */
    public void consommerToken(String token) {
        String sql = "UPDATE password_reset_token SET used = 1 WHERE token = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur PasswordResetDAO.consommerToken : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Nettoyage
    // -------------------------------------------------------

    /**
     * Supprime tous les anciens tokens (expirés ou déjà utilisés) d'un utilisateur.
     * Appelé avant de créer un nouveau token pour éviter les doublons.
     */
    public void supprimerAncienTokens(int userId) {
        String sql = "DELETE FROM password_reset_token "
                   + "WHERE user_id = ? AND (used = 1 OR expires_at < NOW())";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur PasswordResetDAO.supprimerAncienTokens : " + e.getMessage());
        }
    }
}
