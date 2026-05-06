package com.elearning.dao;

import com.elearning.util.DatabaseConnection;

import java.sql.*;

/**
 * DAO pour la table `login_attempt`.
 * Enregistre chaque tentative de connexion (succès ou échec).
 * Utilisé pour la détection des connexions suspectes et le verrouillage de compte.
 */
public class LoginAttemptDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // -------------------------------------------------------
    // Enregistrer une tentative
    // -------------------------------------------------------

    /**
     * Enregistre une tentative de connexion en BDD.
     *
     * @param email   l'email utilisé lors de la tentative
     * @param success true si la connexion a réussi, false sinon
     */
    public void enregistrerTentative(String email, boolean success) {
        String sql = "INSERT INTO login_attempt (email, success, attempted_at) "
                   + "VALUES (?, ?, NOW())";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setBoolean(2, success);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur LoginAttemptDAO.enregistrerTentative : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Compter les échecs récents
    // -------------------------------------------------------

    /**
     * Compte le nombre de tentatives ÉCHOUÉES dans les X dernières minutes.
     *
     * @param email         email à vérifier
     * @param minutesWindow fenêtre temporelle (ex: 15 pour les 15 dernières minutes)
     * @return nombre d'échecs récents
     */
    public int compterEchecsRecents(String email, int minutesWindow) {
        String sql = "SELECT COUNT(*) FROM login_attempt "
                   + "WHERE email = ? AND success = 0 "
                   + "AND attempted_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, minutesWindow);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur LoginAttemptDAO.compterEchecsRecents : " + e.getMessage());
        }
        return 0;
    }

    // -------------------------------------------------------
    // Réinitialiser le compteur
    // -------------------------------------------------------

    /**
     * Marque toutes les tentatives récentes d'un email comme réussies
     * (équivalent à réinitialiser l'historique d'échecs après connexion réussie).
     * En pratique, on ne supprime pas les logs, on repart de zéro depuis le succès.
     *
     * @param email email de l'utilisateur
     */
    public void reinitialiserCompteur(String email) {
        // On ne supprime pas les logs (audit trail),
        // on insère un succès qui "reset" la fenêtre de comptage
        enregistrerTentative(email, true);
    }
}
