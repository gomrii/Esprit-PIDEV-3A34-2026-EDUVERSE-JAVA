package com.elearning.dao;

import com.elearning.entity.GroqGeneratedContent;
import com.elearning.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * GroqGeneratedContentDAO — Accès base de données pour le contenu généré par IA.
 * 
 * Rôle :
 * - Stocker les descriptions générées
 * - Récupérer les générations existantes pour éviter les appels API redondants
 * - Auditer l'utilisation de l'API Groq
 */
public class GroqGeneratedContentDAO {
    
    private static final Logger logger = Logger.getLogger(GroqGeneratedContentDAO.class.getName());

    /**
     * Enregistre un contenu généré par Groq en base de données.
     */
    public int saveGeneratedContent(GroqGeneratedContent content) {
        String sql = "INSERT INTO groq_generated_content " +
                     "(entity_type, entity_id, original_title, generated_description, " +
                     "language, temperature, status, groq_model) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, content.getEntityType());
            stmt.setInt(2, content.getEntityId());
            stmt.setString(3, content.getOriginalTitle());
            stmt.setString(4, content.getGeneratedDescription());
            stmt.setString(5, content.getLanguage());
            stmt.setFloat(6, content.getTemperature());
            stmt.setString(7, content.getStatus());
            stmt.setString(8, content.getGroqModel());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 1) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        content.setId(id);
                        logger.info("Contenu généré enregistré : ID = " + id + 
                                   ", entity = " + content.getEntityType() + "_" + content.getEntityId());
                        return id;
                    }
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL lors de l'enregistrement du contenu généré : " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Récupère un contenu généré par son ID.
     */
    public GroqGeneratedContent getContentById(int id) {
        String sql = "SELECT * FROM groq_generated_content WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToContent(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Récupère tous les contenus générés pour une entité.
     */
    public List<GroqGeneratedContent> getContentsByEntity(String entityType, int entityId) {
        String sql = "SELECT * FROM groq_generated_content " +
                     "WHERE entity_type = ? AND entity_id = ? " +
                     "ORDER BY created_at DESC";
        List<GroqGeneratedContent> contents = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, entityType);
            stmt.setInt(2, entityId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contents.add(mapRowToContent(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return contents;
    }

    /**
     * Récupère le dernier contenu généré pour une entité.
     * Utile pour éviter de régénérer si déjà existant.
     */
    public GroqGeneratedContent getLatestContentForEntity(String entityType, int entityId) {
        String sql = "SELECT * FROM groq_generated_content " +
                     "WHERE entity_type = ? AND entity_id = ? AND status = ? " +
                     "ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, entityType);
            stmt.setInt(2, entityId);
            stmt.setString(3, GroqGeneratedContent.STATUS_COMPLETED);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToContent(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Récupère tous les contenus par statut.
     */
    public List<GroqGeneratedContent> getContentsByStatus(String status) {
        String sql = "SELECT * FROM groq_generated_content " +
                     "WHERE status = ? ORDER BY created_at DESC";
        List<GroqGeneratedContent> contents = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contents.add(mapRowToContent(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return contents;
    }

    /**
     * Met à jour le statut d'un contenu généré.
     */
    public boolean updateContentStatus(int id, String status) {
        String sql = "UPDATE groq_generated_content SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Contenu généré mis à jour : ID = " + id + ", Status = " + status);
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Met à jour la description générée.
     */
    public boolean updateGeneratedDescription(int id, String description) {
        String sql = "UPDATE groq_generated_content SET generated_description = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, description);
            stmt.setString(2, GroqGeneratedContent.STATUS_COMPLETED);
            stmt.setInt(3, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Récupère les statistiques d'utilisation de l'API Groq.
     */
    public GroqUsageStats getUsageStats() {
        String sqlTotal = "SELECT COUNT(*) as total FROM groq_generated_content";
        String sqlCompleted = "SELECT COUNT(*) as completed FROM groq_generated_content WHERE status = ?";
        String sqlFailed = "SELECT COUNT(*) as failed FROM groq_generated_content WHERE status = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            
            // Total
            try (PreparedStatement stmt = conn.prepareStatement(sqlTotal);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    
                    // Completed
                    try (PreparedStatement stmt2 = conn.prepareStatement(sqlCompleted)) {
                        stmt2.setString(1, GroqGeneratedContent.STATUS_COMPLETED);
                        try (ResultSet rs2 = stmt2.executeQuery()) {
                            int completed = rs2.next() ? rs2.getInt("completed") : 0;
                            
                            // Failed
                            try (PreparedStatement stmt3 = conn.prepareStatement(sqlFailed)) {
                                stmt3.setString(1, GroqGeneratedContent.STATUS_FAILED);
                                try (ResultSet rs3 = stmt3.executeQuery()) {
                                    int failed = rs3.next() ? rs3.getInt("failed") : 0;
                                    
                                    return new GroqUsageStats(total, completed, failed);
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        
        return new GroqUsageStats(0, 0, 0);
    }

    /**
     * Supprime un contenu généré.
     */
    public boolean deleteContent(int id) {
        String sql = "DELETE FROM groq_generated_content WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            logger.severe("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===============================================
    // Helper Methods
    // ===============================================

    private GroqGeneratedContent mapRowToContent(ResultSet rs) throws SQLException {
        GroqGeneratedContent content = new GroqGeneratedContent();
        content.setId(rs.getInt("id"));
        content.setEntityType(rs.getString("entity_type"));
        content.setEntityId(rs.getInt("entity_id"));
        content.setOriginalTitle(rs.getString("original_title"));
        content.setGeneratedDescription(rs.getString("generated_description"));
        content.setLanguage(rs.getString("language"));
        content.setTemperature(rs.getFloat("temperature"));
        content.setStatus(rs.getString("status"));
        content.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        content.setGroqModel(rs.getString("groq_model"));
        
        return content;
    }

    // ===============================================
    // Inner Classes
    // ===============================================

    public static class GroqUsageStats {
        public final int totalRequests;
        public final int completedRequests;
        public final int failedRequests;

        public GroqUsageStats(int total, int completed, int failed) {
            this.totalRequests = total;
            this.completedRequests = completed;
            this.failedRequests = failed;
        }

        public double getSuccessRate() {
            if (totalRequests == 0) return 0.0;
            return (completedRequests * 100.0) / totalRequests;
        }

        @Override
        public String toString() {
            return String.format(
                "GroqUsageStats{total=%d, completed=%d, failed=%d, successRate=%.1f%%}",
                totalRequests, completedRequests, failedRequests, getSuccessRate()
            );
        }
    }
}
