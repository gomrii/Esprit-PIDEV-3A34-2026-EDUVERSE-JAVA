package com.elearning.entity;

import java.time.LocalDateTime;

/**
 * FormationEnrollment — Represents a student's enrollment in a formation.
 */
public class FormationEnrollment {
    private int id;
    private int formationId;
    private int studentId;
    private LocalDateTime enrolledAt;

    // Constructor
    public FormationEnrollment() {
        this.enrolledAt = LocalDateTime.now();
    }

    public FormationEnrollment(int formationId, int studentId) {
        this.formationId = formationId;
        this.studentId = studentId;
        this.enrolledAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFormationId() {
        return formationId;
    }

    public void setFormationId(int formationId) {
        this.formationId = formationId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    @Override
    public String toString() {
        return "FormationEnrollment{" +
                "id=" + id +
                ", formationId=" + formationId +
                ", studentId=" + studentId +
                ", enrolledAt=" + enrolledAt +
                '}';
    }
}
