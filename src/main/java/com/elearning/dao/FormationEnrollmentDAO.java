package com.elearning.dao;

import com.elearning.entity.FormationEnrollment;
import com.elearning.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FormationEnrollmentDAO — Data Access Object for FormationEnrollment.
 */
public class FormationEnrollmentDAO {

    /**
     * Creates a new enrollment record in the database.
     */
    public boolean create(FormationEnrollment enrollment) {
        String sql = "INSERT INTO formation_enrollment (formation_id, student_id, enrolled_at) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, enrollment.getFormationId());
            stmt.setInt(2, enrollment.getStudentId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de l'inscription: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a student is already enrolled in a formation.
     */
    public boolean isEnrolled(int formationId, int studentId) {
        String sql = "SELECT COUNT(*) FROM formation_enrollment WHERE formation_id = ? AND student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, formationId);
            stmt.setInt(2, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all enrollments for a specific student.
     */
    public List<Integer> getEnrolledFormationIds(int studentId) {
        List<Integer> formationIds = new ArrayList<>();
        String sql = "SELECT formation_id FROM formation_enrollment WHERE student_id = ? ORDER BY enrolled_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    formationIds.add(rs.getInt("formation_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des inscriptions: " + e.getMessage());
            e.printStackTrace();
        }
        return formationIds;
    }

    /**
     * Retrieves all students enrolled in a specific formation.
     */
    public List<Integer> getEnrolledStudentIds(int formationId) {
        List<Integer> studentIds = new ArrayList<>();
        String sql = "SELECT student_id FROM formation_enrollment WHERE formation_id = ? ORDER BY enrolled_at ASC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, formationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    studentIds.add(rs.getInt("student_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des étudiants: " + e.getMessage());
            e.printStackTrace();
        }
        return studentIds;
    }

    /**
     * Deletes an enrollment record (unenroll a student).
     */
    public boolean delete(int formationId, int studentId) {
        String sql = "DELETE FROM formation_enrollment WHERE formation_id = ? AND student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, formationId);
            stmt.setInt(2, studentId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'inscription: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Counts total enrollments for a formation.
     */
    public int countEnrollments(int formationId) {
        String sql = "SELECT COUNT(*) FROM formation_enrollment WHERE formation_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, formationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des inscriptions: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}
