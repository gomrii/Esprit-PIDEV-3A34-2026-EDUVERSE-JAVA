package com.elearning.service;

import com.elearning.dao.FormationEnrollmentDAO;
import com.elearning.entity.FormationEnrollment;

import java.util.List;

/**
 * FormationEnrollmentService — Business logic for formation enrollment/subscription.
 */
public class FormationEnrollmentService {
    private final FormationEnrollmentDAO enrollmentDAO = new FormationEnrollmentDAO();

    /**
     * Enroll a student in a formation.
     *
     * @param formationId The ID of the formation
     * @param studentId The ID of the student
     * @return true if enrollment was successful, false if already enrolled or error
     */
    public boolean enrollStudent(int formationId, int studentId) {
        // Check if already enrolled
        if (isStudentEnrolled(formationId, studentId)) {
            System.out.println("⚠️ L'étudiant est déjà inscrit à cette formation.");
            return false;
        }

        // Create enrollment record
        FormationEnrollment enrollment = new FormationEnrollment(formationId, studentId);
        boolean success = enrollmentDAO.create(enrollment);

        if (success) {
            System.out.println("✓ Inscription réussie ! Étudiant " + studentId + " inscrit à la formation " + formationId);
        } else {
            System.out.println("❌ Erreur lors de l'inscription.");
        }

        return success;
    }

    /**
     * Check if a student is enrolled in a formation.
     */
    public boolean isStudentEnrolled(int formationId, int studentId) {
        return enrollmentDAO.isEnrolled(formationId, studentId);
    }

    /**
     * Get all formations a student is enrolled in.
     */
    public List<Integer> getStudentFormations(int studentId) {
        return enrollmentDAO.getEnrolledFormationIds(studentId);
    }

    /**
     * Get all students enrolled in a formation.
     */
    public List<Integer> getFormationStudents(int formationId) {
        return enrollmentDAO.getEnrolledStudentIds(formationId);
    }

    /**
     * Unenroll a student from a formation.
     */
    public boolean unenrollStudent(int formationId, int studentId) {
        boolean success = enrollmentDAO.delete(formationId, studentId);

        if (success) {
            System.out.println("✓ Désinscription réussie ! Étudiant " + studentId + " désinscrit de la formation " + formationId);
        } else {
            System.out.println("❌ Erreur lors de la désinscription.");
        }

        return success;
    }

    /**
     * Get total number of students enrolled in a formation.
     */
    public int getEnrollmentCount(int formationId) {
        return enrollmentDAO.countEnrollments(formationId);
    }
}
