package edu.connexion3a36.services;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardStatsService {
    private static final String VALID_STATUS_CONDITION = "LOWER(TRIM(statut)) IN ('valide', 'validé', 'valid')";
    private static final String PENDING_STATUS_CONDITION = "LOWER(TRIM(statut)) IN ('en_attente', 'en attente', 'attente', 'pending')";
    private static final String REJECTED_STATUS_CONDITION = "LOWER(TRIM(statut)) IN ('rejete', 'rejeté', 'rejected')";
    private static final String TEACHER_CREATED_BY_CONDITION = "LOWER(TRIM(created_by)) IN ('teacher', 'enseignant')";

    private static final List<StudentAttempt> STUDENT_ATTEMPTS = new ArrayList<>();

    private final Connection cnx;

    public DashboardStatsService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public AdminStats fetchAdminStats() throws SQLException {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM quiz) AS total_quiz, " +
                "(SELECT COUNT(*) FROM quiz WHERE " + VALID_STATUS_CONDITION + ") AS validated_quiz, " +
                "(SELECT COUNT(*) FROM quiz WHERE " + PENDING_STATUS_CONDITION + ") AS pending_quiz, " +
                "(SELECT COUNT(*) FROM quiz WHERE " + REJECTED_STATUS_CONDITION + ") AS rejected_quiz, " +
                "(SELECT COUNT(*) FROM question) AS total_questions, " +
                "(SELECT COUNT(*) FROM reponse) AS total_reponses, " +
                "(SELECT COUNT(*) FROM quiz WHERE LOWER(TRIM(created_by)) = 'admin') AS admin_created_quiz, " +
                "(SELECT COUNT(*) FROM quiz WHERE " + TEACHER_CREATED_BY_CONDITION + ") AS teacher_created_quiz";

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return new AdminStats(
                        rs.getInt("total_quiz"),
                        rs.getInt("validated_quiz"),
                        rs.getInt("pending_quiz"),
                        rs.getInt("rejected_quiz"),
                        rs.getInt("total_questions"),
                        rs.getInt("total_reponses"),
                        rs.getInt("admin_created_quiz"),
                        rs.getInt("teacher_created_quiz")
                );
            }
        }

        return new AdminStats();
    }

    public TeacherStats fetchTeacherStats() throws SQLException {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM quiz WHERE " + TEACHER_CREATED_BY_CONDITION + ") AS total_quiz, " +
                "(SELECT COUNT(*) FROM quiz WHERE " + TEACHER_CREATED_BY_CONDITION + " AND " + VALID_STATUS_CONDITION + ") AS validated_quiz, " +
                "(SELECT COUNT(*) FROM quiz WHERE " + TEACHER_CREATED_BY_CONDITION + " AND " + PENDING_STATUS_CONDITION + ") AS pending_quiz, " +
                "(SELECT COUNT(*) FROM quiz WHERE " + TEACHER_CREATED_BY_CONDITION + " AND " + REJECTED_STATUS_CONDITION + ") AS rejected_quiz, " +
                "(SELECT COUNT(*) FROM question q INNER JOIN quiz z ON q.id_quiz = z.id_quiz WHERE " + TEACHER_CREATED_BY_CONDITION.replace("created_by", "z.created_by") + ") AS total_questions, " +
                "(SELECT COUNT(*) FROM reponse r INNER JOIN question q ON r.id_question = q.id_question INNER JOIN quiz z ON q.id_quiz = z.id_quiz WHERE " + TEACHER_CREATED_BY_CONDITION.replace("created_by", "z.created_by") + ") AS total_reponses";

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return new TeacherStats(
                        rs.getInt("total_quiz"),
                        rs.getInt("validated_quiz"),
                        rs.getInt("pending_quiz"),
                        rs.getInt("rejected_quiz"),
                        rs.getInt("total_questions"),
                        rs.getInt("total_reponses")
                );
            }
        }

        return new TeacherStats();
    }

    public StudentStats fetchStudentStats() throws SQLException {
        int availableValidQuiz = countValidQuizzes();
        synchronized (STUDENT_ATTEMPTS) {
            StudentAttempt bestAttempt = STUDENT_ATTEMPTS.stream()
                    .max(Comparator.comparingDouble(StudentAttempt::percentage)
                            .thenComparing(StudentAttempt::createdAt))
                    .orElse(null);
            StudentAttempt lastAttempt = STUDENT_ATTEMPTS.stream()
                    .max(Comparator.comparing(StudentAttempt::createdAt))
                    .orElse(null);

            double averagePercentage = STUDENT_ATTEMPTS.stream()
                    .mapToDouble(StudentAttempt::percentage)
                    .average()
                    .orElse(0);

            long successfulQuizCount = STUDENT_ATTEMPTS.stream()
                    .filter(StudentAttempt::successful)
                    .count();

            return new StudentStats(
                    availableValidQuiz,
                    STUDENT_ATTEMPTS.size(),
                    formatAttempt(bestAttempt),
                    formatAttempt(lastAttempt),
                    formatPercentage(averagePercentage),
                    (int) successfulQuizCount
            );
        }
    }

    public void recordStudentAttempt(Quiz quiz, double score, double totalPossibleScore) {
        StudentAttempt attempt = new StudentAttempt(
                quiz != null ? quiz.getTitre() : "Quiz",
                score,
                totalPossibleScore,
                totalPossibleScore > 0 ? (score / totalPossibleScore) * 100 : 0,
                totalPossibleScore > 0 && ((score / totalPossibleScore) * 100) >= 50,
                LocalDateTime.now()
        );
        synchronized (STUDENT_ATTEMPTS) {
            STUDENT_ATTEMPTS.add(attempt);
        }
    }

    private int countValidQuizzes() throws SQLException {
        String query = "SELECT COUNT(*) FROM quiz WHERE " + VALID_STATUS_CONDITION;
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String formatAttempt(StudentAttempt attempt) {
        if (attempt == null) {
            return "0 / 0";
        }
        return formatScore(attempt.score()) + " / " + formatScore(attempt.totalPossibleScore());
    }

    private String formatPercentage(double value) {
        if (Math.floor(value) == value) {
            return (int) value + " %";
        }
        return String.format(Locale.US, "%.2f %%", value);
    }

    private String formatScore(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private record StudentAttempt(String quizTitle, double score, double totalPossibleScore, double percentage,
                                  boolean successful, LocalDateTime createdAt) {
    }

    public static final class AdminStats {
        private final int totalQuiz;
        private final int validatedQuiz;
        private final int pendingQuiz;
        private final int rejectedQuiz;
        private final int totalQuestions;
        private final int totalResponses;
        private final int adminCreatedQuiz;
        private final int teacherCreatedQuiz;

        public AdminStats() {
            this(0, 0, 0, 0, 0, 0, 0, 0);
        }

        public AdminStats(int totalQuiz, int validatedQuiz, int pendingQuiz, int rejectedQuiz,
                          int totalQuestions, int totalResponses, int adminCreatedQuiz, int teacherCreatedQuiz) {
            this.totalQuiz = totalQuiz;
            this.validatedQuiz = validatedQuiz;
            this.pendingQuiz = pendingQuiz;
            this.rejectedQuiz = rejectedQuiz;
            this.totalQuestions = totalQuestions;
            this.totalResponses = totalResponses;
            this.adminCreatedQuiz = adminCreatedQuiz;
            this.teacherCreatedQuiz = teacherCreatedQuiz;
        }

        public int getTotalQuiz() {
            return totalQuiz;
        }

        public int getValidatedQuiz() {
            return validatedQuiz;
        }

        public int getPendingQuiz() {
            return pendingQuiz;
        }

        public int getRejectedQuiz() {
            return rejectedQuiz;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public int getTotalResponses() {
            return totalResponses;
        }

        public int getAdminCreatedQuiz() {
            return adminCreatedQuiz;
        }

        public int getTeacherCreatedQuiz() {
            return teacherCreatedQuiz;
        }
    }

    public static final class TeacherStats {
        private final int totalQuiz;
        private final int validatedQuiz;
        private final int pendingQuiz;
        private final int rejectedQuiz;
        private final int totalQuestions;
        private final int totalResponses;

        public TeacherStats() {
            this(0, 0, 0, 0, 0, 0);
        }

        public TeacherStats(int totalQuiz, int validatedQuiz, int pendingQuiz, int rejectedQuiz, int totalQuestions, int totalResponses) {
            this.totalQuiz = totalQuiz;
            this.validatedQuiz = validatedQuiz;
            this.pendingQuiz = pendingQuiz;
            this.rejectedQuiz = rejectedQuiz;
            this.totalQuestions = totalQuestions;
            this.totalResponses = totalResponses;
        }

        public int getTotalQuiz() {
            return totalQuiz;
        }

        public int getValidatedQuiz() {
            return validatedQuiz;
        }

        public int getPendingQuiz() {
            return pendingQuiz;
        }

        public int getRejectedQuiz() {
            return rejectedQuiz;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public int getTotalResponses() {
            return totalResponses;
        }
    }

    public static final class StudentStats {
        private final int availableValidQuiz;
        private final int passedQuizCount;
        private final String bestScore;
        private final String lastScore;
        private final String averageScore;
        private final int successfulQuizCount;

        public StudentStats() {
            this(0, 0, "0 / 0", "0 / 0", "0 %", 0);
        }

        public StudentStats(int availableValidQuiz, int passedQuizCount, String bestScore,
                            String lastScore, String averageScore, int successfulQuizCount) {
            this.availableValidQuiz = availableValidQuiz;
            this.passedQuizCount = passedQuizCount;
            this.bestScore = bestScore;
            this.lastScore = lastScore;
            this.averageScore = averageScore;
            this.successfulQuizCount = successfulQuizCount;
        }

        public int getAvailableValidQuiz() {
            return availableValidQuiz;
        }

        public int getPassedQuizCount() {
            return passedQuizCount;
        }

        public String getBestScore() {
            return bestScore;
        }

        public String getLastScore() {
            return lastScore;
        }

        public String getAverageScore() {
            return averageScore;
        }

        public int getSuccessfulQuizCount() {
            return successfulQuizCount;
        }
    }
}
