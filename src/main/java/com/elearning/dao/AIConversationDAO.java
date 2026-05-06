package com.elearning.dao;

import com.elearning.entity.AIConversation;
import com.elearning.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table `ai_conversation`.
 * Sauvegarde et récupère l'historique des échanges avec l'assistant IA.
 */
public class AIConversationDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // -------------------------------------------------------
    // Sauvegarder une conversation
    // -------------------------------------------------------

    /**
     * Insère un échange question/réponse avec les données de contexte BDD.
     *
     * @param adminId     ID de l'admin qui a posé la question
     * @param question    texte de la question
     * @param reponse     réponse de Claude
     * @param contextJson JSON des stats BDD au moment de la question
     */
    public void sauvegarder(int adminId, String question, String reponse, String contextJson) {
        String sql = "INSERT INTO ai_conversation (admin_id, question, reponse, context_data, created_at) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setString(2, question);
            ps.setString(3, reponse);
            ps.setString(4, contextJson);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur AIConversationDAO.sauvegarder : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Récupérer l'historique
    // -------------------------------------------------------

    /**
     * Retourne les N dernières conversations d'un admin, du plus récent au plus ancien.
     *
     * @param adminId ID de l'admin
     * @param limit   nombre maximum de conversations à retourner
     * @return liste des conversations
     */
    public List<AIConversation> getHistorique(int adminId, int limit) {
        String sql = "SELECT * FROM ai_conversation WHERE admin_id = ? "
                   + "ORDER BY created_at DESC LIMIT ?";
        List<AIConversation> liste = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AIConversation conv = new AIConversation();
                    conv.setId(rs.getInt("id"));
                    conv.setAdminId(rs.getInt("admin_id"));
                    conv.setQuestion(rs.getString("question"));
                    conv.setReponse(rs.getString("reponse"));
                    conv.setContextData(rs.getString("context_data"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) conv.setCreatedAt(ts.toLocalDateTime());
                    liste.add(conv);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur AIConversationDAO.getHistorique : " + e.getMessage());
        }
        return liste;
    }

    // -------------------------------------------------------
    // Compter
    // -------------------------------------------------------

    /** Retourne le nombre total de conversations pour un admin. */
    public int compterParAdmin(int adminId) {
        String sql = "SELECT COUNT(*) FROM ai_conversation WHERE admin_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur AIConversationDAO.compterParAdmin : " + e.getMessage());
        }
        return 0;
    }
}
