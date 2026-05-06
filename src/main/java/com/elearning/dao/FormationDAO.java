package com.elearning.dao;

import com.elearning.entity.Formation;
import com.elearning.entity.User;
import com.elearning.util.DatabaseConnection;
import com.elearning.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FormationDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int create(Formation formation) {
        String sql = "INSERT INTO formation (title, description, content, price, is_approved, is_archived, created_at, support_file, duration, level, creator_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, formation.getTitle());
            ps.setString(2, formation.getDescription());
            ps.setString(3, formation.getContent());
            ps.setDouble(4, formation.getPrice());
            ps.setBoolean(5, formation.isApproved());
            ps.setBoolean(6, formation.isArchived());
            ps.setTimestamp(7, Timestamp.valueOf(formation.getCreatedAt()));
            ps.setString(8, formation.getSupportFile());
            ps.setInt(9, formation.getDuration());
            ps.setString(10, formation.getLevel());
            ps.setInt(11, resolveCreatorId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur creation formation: " + e.getMessage(), e);
        }

        return -1;
    }

    private int resolveCreatorId() {
        User current = SessionManager.getInstance().getUtilisateurConnecte();
        if (current != null && current.getId() > 0) {
            return current.getId();
        }
        return 1;
    }

    public List<Formation> findAll() {
        String sql = "SELECT f.*, u.full_name AS creator_name FROM formation f " +
                "LEFT JOIN user u ON u.id = f.creator_id ORDER BY f.id DESC";
        List<Formation> formations = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                formations.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture formations: " + e.getMessage(), e);
        }

        return formations;
    }

    public List<Formation> findAvailableForStudent() {
        String sql = "SELECT f.*, u.full_name AS creator_name FROM formation f " +
                "LEFT JOIN user u ON u.id = f.creator_id " +
                "WHERE f.is_approved = 1 AND f.is_archived = 0 ORDER BY f.id DESC";
        List<Formation> formations = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                formations.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture formations etudiant: " + e.getMessage(), e);
        }
        return formations;
    }

    public List<Formation> findForTeacherView() {
        String sql = "SELECT f.*, u.full_name AS creator_name FROM formation f " +
                "LEFT JOIN user u ON u.id = f.creator_id " +
                "WHERE u.role IN ('ADMIN', 'ENSEIGNANT') ORDER BY f.id DESC";
        List<Formation> formations = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                formations.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture formations enseignant: " + e.getMessage(), e);
        }
        return formations;
    }

    public List<Formation> searchForTeacherView(String searchText, String levelFilter, String sortColumn, String sortDir) {
        return searchWithBaseCondition("u.role IN ('ADMIN', 'ENSEIGNANT')", searchText, levelFilter, sortColumn, sortDir);
    }

    public List<Formation> searchAvailableForStudent(String searchText, String levelFilter, String sortColumn, String sortDir) {
        return searchWithBaseCondition("f.is_approved = 1 AND f.is_archived = 0", searchText, levelFilter, sortColumn, sortDir);
    }

    public List<Formation> searchEnrolledForStudent(int studentId, String searchText, String levelFilter, String sortColumn, String sortDir) {
        Map<String, String> allowedSorts = new LinkedHashMap<>();
        allowedSorts.put("id", "f.id");
        allowedSorts.put("title", "f.title");
        allowedSorts.put("price", "f.price");
        allowedSorts.put("level", "f.level");
        allowedSorts.put("duration", "f.duration");
        allowedSorts.put("created_at", "f.created_at");
        String safeColumn = allowedSorts.getOrDefault(sortColumn, "f.id");
        String safeDir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        StringBuilder sql = new StringBuilder(
                "SELECT f.*, u.full_name AS creator_name FROM formation f " +
                "LEFT JOIN user u ON u.id = f.creator_id " +
                "JOIN user_formation fe ON fe.formation_id = f.id " +
                "WHERE fe.user_id = ? AND f.is_archived = 0");

        List<Object> params = new ArrayList<>();
        params.add(studentId);

        if (searchText != null && !searchText.isBlank()) {
            sql.append(" AND (f.title LIKE ? OR f.description LIKE ?)");
            String value = "%" + searchText.trim() + "%";
            params.add(value);
            params.add(value);
        }
        if (levelFilter != null && !levelFilter.isBlank()) {
            sql.append(" AND f.level = ?");
            params.add(levelFilter);
        }
        sql.append(" ORDER BY ").append(safeColumn).append(" ").append(safeDir);

        List<Formation> formations = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    formations.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche formations inscrites: " + e.getMessage(), e);
        }
        return formations;
    }

    public List<Formation> rechercherFormations(String searchText, String levelFilter, String sortColumn, String sortDir) {
        Map<String, String> allowedSorts = new LinkedHashMap<>();
        allowedSorts.put("id", "id");
        allowedSorts.put("title", "title");
        allowedSorts.put("price", "price");
        allowedSorts.put("level", "level");
        allowedSorts.put("duration", "duration");
        allowedSorts.put("is_approved", "is_approved");
        allowedSorts.put("created_at", "created_at");

        String safeColumn = allowedSorts.getOrDefault(sortColumn, "id");
        String safeDir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";

        StringBuilder sql = new StringBuilder("SELECT * FROM formation WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.isBlank()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            String search = "%" + searchText.trim() + "%";
            params.add(search);
            params.add(search);
        }

        if (levelFilter != null && !levelFilter.isBlank()) {
            sql.append(" AND level = ?");
            params.add(levelFilter);
        }

        sql.append(" ORDER BY ").append(safeColumn).append(" ").append(safeDir);

        List<Formation> formations = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    formations.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche formations: " + e.getMessage(), e);
        }
        return formations;
    }

    public boolean update(Formation formation) {
        String sql = "UPDATE formation SET title=?, description=?, content=?, price=?, is_approved=?, is_archived=?, support_file=?, duration=?, level=? WHERE id=?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, formation.getTitle());
            ps.setString(2, formation.getDescription());
            ps.setString(3, formation.getContent());
            ps.setDouble(4, formation.getPrice());
            ps.setBoolean(5, formation.isApproved());
            ps.setBoolean(6, formation.isArchived());
            ps.setString(7, formation.getSupportFile());
            ps.setInt(8, formation.getDuration());
            ps.setString(9, formation.getLevel());
            ps.setInt(10, formation.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification formation: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM formation WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression formation: " + e.getMessage(), e);
        }
    }

    public boolean setApprovalState(int formationId, boolean approved, boolean archived) {
        String sql = "UPDATE formation SET is_approved=?, is_archived=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, approved);
            ps.setBoolean(2, archived);
            ps.setInt(3, formationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise a jour approval formation: " + e.getMessage(), e);
        }
    }

    public boolean enrollStudent(int formationId, int studentId) {
        String sql = "INSERT INTO user_formation (formation_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, formationId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                return false;
            }
            throw new RuntimeException("Erreur inscription formation: " + e.getMessage(), e);
        }
    }

    public boolean isStudentEnrolled(int formationId, int studentId) {
        String sql = "SELECT COUNT(*) FROM user_formation WHERE formation_id=? AND user_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, formationId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur verification inscription formation: " + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> findEnrolledStudentsForTeacher(int teacherId) {
        String sql = "SELECT f.title AS formation_title, u.full_name AS student_name, u.email AS student_email " +
                "FROM user_formation fe " +
                "JOIN formation f ON f.id = fe.formation_id " +
                "JOIN user u ON u.id = fe.user_id " +
                "WHERE f.creator_id = ? " +
                "ORDER BY f.title ASC";
        List<Map<String, String>> rows = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("formationTitle", rs.getString("formation_title"));
                    row.put("studentName", rs.getString("student_name"));
                    row.put("studentEmail", rs.getString("student_email"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture etudiants inscrits: " + e.getMessage(), e);
        }
        return rows;
    }

    public List<Map<String, String>> getEnrolledStudentsForFormation(int formationId) {
        String sql = "SELECT u.full_name AS student_name, u.email AS student_email " +
                "FROM user_formation fe " +
                "JOIN user u ON u.id = fe.user_id " +
                "WHERE fe.formation_id = ? " +
                "ORDER BY u.full_name ASC";
        List<Map<String, String>> rows = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("studentName", rs.getString("student_name"));
                    row.put("studentEmail", rs.getString("student_email"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture etudiants inscrits: " + e.getMessage(), e);
        }
        return rows;
    }

    private List<Formation> searchWithBaseCondition(String baseCondition, String searchText, String levelFilter, String sortColumn, String sortDir) {
        Map<String, String> allowedSorts = new LinkedHashMap<>();
        allowedSorts.put("id", "f.id");
        allowedSorts.put("title", "f.title");
        allowedSorts.put("price", "f.price");
        allowedSorts.put("level", "f.level");
        allowedSorts.put("duration", "f.duration");
        allowedSorts.put("is_approved", "f.is_approved");
        allowedSorts.put("created_at", "f.created_at");

        String safeColumn = allowedSorts.getOrDefault(sortColumn, "f.id");
        String safeDir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        StringBuilder sql = new StringBuilder("SELECT f.*, u.full_name AS creator_name FROM formation f " +
                "LEFT JOIN user u ON u.id = f.creator_id WHERE " + baseCondition);
        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.isBlank()) {
            sql.append(" AND (f.title LIKE ? OR f.description LIKE ?)");
            String value = "%" + searchText.trim() + "%";
            params.add(value);
            params.add(value);
        }
        if (levelFilter != null && !levelFilter.isBlank()) {
            sql.append(" AND f.level = ?");
            params.add(levelFilter);
        }
        sql.append(" ORDER BY ").append(safeColumn).append(" ").append(safeDir);

        List<Formation> formations = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    formations.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche formations: " + e.getMessage(), e);
        }
        return formations;
    }

    private Formation map(ResultSet rs) throws SQLException {
        Formation formation = new Formation();
        formation.setId(rs.getInt("id"));
        formation.setTitle(rs.getString("title"));
        formation.setDescription(rs.getString("description"));
        formation.setContent(rs.getString("content"));
        formation.setPrice(rs.getDouble("price"));
        formation.setApproved(rs.getBoolean("is_approved"));
        formation.setArchived(rs.getBoolean("is_archived"));
        formation.setSupportFile(rs.getString("support_file"));
        formation.setDuration(rs.getInt("duration"));
        formation.setLevel(rs.getString("level"));
        formation.setCreatorId(rs.getInt("creator_id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            formation.setCreatedAt(createdAt.toLocalDateTime());
        }

        try {
            formation.setCreatorName(rs.getString("creator_name"));
        } catch (SQLException ignored) {
            formation.setCreatorName(null);
        }

        // Load ressources for this formation
        loadRessources(formation);

        return formation;
    }

    /**
     * Load ressources for a formation
     */
    private void loadRessources(Formation formation) {
        RessourceDAO ressourceDAO = new RessourceDAO();
        formation.setRessources(ressourceDAO.findByFormationId(formation.getId()));
    }
}
