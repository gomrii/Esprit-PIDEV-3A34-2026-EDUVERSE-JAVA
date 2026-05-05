package com.elearning.dao;

import com.elearning.entity.AIRecommendation;
import com.elearning.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AIRecommendationDAO {

    public void sauvegarderRecommandations(List<AIRecommendation> recommandations) {
        String sql = "INSERT INTO ai_recommendation (priorite, type, titre, description, action_suggeree, lue, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            for (AIRecommendation rec : recommandations) {
                pstmt.setString(1, rec.getPriorite());
                pstmt.setString(2, rec.getType());
                pstmt.setString(3, rec.getTitre());
                pstmt.setString(4, rec.getDescription());
                pstmt.setString(5, rec.getActionSuggeree());
                pstmt.setBoolean(6, rec.isLue());
                pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde IA recommandations : " + e.getMessage());
        }
    }

    public List<AIRecommendation> getRecommandationsNonLues() {
        List<AIRecommendation> liste = new ArrayList<>();
        String sql = "SELECT * FROM ai_recommendation WHERE lue = 0 ORDER BY priorite DESC, created_at DESC LIMIT 10";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AIRecommendation rec = new AIRecommendation();
                rec.setId(rs.getInt("id"));
                rec.setPriorite(rs.getString("priorite"));
                rec.setType(rs.getString("type"));
                rec.setTitre(rs.getString("titre"));
                rec.setDescription(rs.getString("description"));
                rec.setActionSuggeree(rs.getString("action_suggeree"));
                rec.setLue(rs.getBoolean("lue"));
                rec.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                liste.add(rec);
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture IA recommandations : " + e.getMessage());
        }
        return liste;
    }

    public void marquerCommeLue(int id) {
        String sql = "UPDATE ai_recommendation SET lue = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erreur update IA recommandation : " + e.getMessage());
        }
    }

    public void supprimerAnciennesRecommandations() {
        String sql = "DELETE FROM ai_recommendation WHERE id NOT IN (" +
                     "  SELECT id FROM (SELECT id FROM ai_recommendation ORDER BY id DESC LIMIT 20) as t" +
                     ")";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("Erreur suppression IA recommandations : " + e.getMessage());
        }
    }
}
