package com.elearning.dao;

import com.elearning.entity.Ressource;
import com.elearning.util.DatabaseConnection;
import com.elearning.util.SessionManager;
import com.elearning.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RessourceDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Create a new ressource for a formation
     */
    public int create(Ressource ressource) {
        String sql = "INSERT INTO ressource (title, description, url, type, file_path, cost, formation_id, created_by_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ressource.getTitle());
            ps.setString(2, ressource.getDescription());
            ps.setString(3, ressource.getUrl());
            ps.setString(4, ressource.getType());
            ps.setString(5, ressource.getFilePath());
            ps.setObject(6, ressource.getCost());
            ps.setInt(7, ressource.getFormationId());
            ps.setInt(8, resolveCreatorId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur création ressource: " + e.getMessage(), e);
        }

        return -1;
    }

    /**
     * Find all ressources for a specific formation
     */
    public List<Ressource> findByFormationId(int formationId) {
        String sql = "SELECT * FROM ressource WHERE formation_id = ? ORDER BY id ASC";
        List<Ressource> ressources = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ressources.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture ressources: " + e.getMessage(), e);
        }

        return ressources;
    }

    /**
     * Find a specific ressource by id
     */
    public Ressource findById(int id) {
        String sql = "SELECT * FROM ressource WHERE id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture ressource: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Update an existing ressource
     */
    public boolean update(Ressource ressource) {
        String sql = "UPDATE ressource SET title = ?, description = ?, url = ?, type = ?, file_path = ?, cost = ?, updated_at = NOW(), updated_by_id = ? WHERE id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, ressource.getTitle());
            ps.setString(2, ressource.getDescription());
            ps.setString(3, ressource.getUrl());
            ps.setString(4, ressource.getType());
            ps.setString(5, ressource.getFilePath());
            ps.setObject(6, ressource.getCost());
            ps.setInt(7, resolveCreatorId());
            ps.setInt(8, ressource.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour ressource: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a ressource by id
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM ressource WHERE id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression ressource: " + e.getMessage(), e);
        }
    }

    /**
     * Delete all ressources for a formation
     */
    public boolean deleteByFormationId(int formationId) {
        String sql = "DELETE FROM ressource WHERE formation_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, formationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression ressources formation: " + e.getMessage(), e);
        }
    }

    /**
     * Map ResultSet to Ressource object
     */
    private Ressource map(ResultSet rs) throws SQLException {
        Ressource ressource = new Ressource();
        ressource.setId(rs.getInt("id"));
        ressource.setTitle(rs.getString("title"));
        ressource.setDescription(rs.getString("description"));
        ressource.setUrl(rs.getString("url"));
        ressource.setType(rs.getString("type"));
        ressource.setFilePath(rs.getString("file_path"));
        
        Object cost = rs.getObject("cost");
        if (cost != null) {
            ressource.setCost(((Number) cost).doubleValue());
        }
        
        ressource.setFormationId(rs.getInt("formation_id"));
        ressource.setCreatedById(rs.getInt("created_by_id"));
        
        Object updatedById = rs.getObject("updated_by_id");
        if (updatedById != null) {
            ressource.setUpdatedById(((Number) updatedById).intValue());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            ressource.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            ressource.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return ressource;
    }

    /**
     * Get current user ID for created_by tracking
     */
    private int resolveCreatorId() {
        User current = SessionManager.getInstance().getUtilisateurConnecte();
        if (current != null && current.getId() > 0) {
            return current.getId();
        }
        return 1;
    }
}
