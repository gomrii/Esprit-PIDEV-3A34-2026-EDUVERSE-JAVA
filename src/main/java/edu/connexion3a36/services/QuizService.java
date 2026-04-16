package edu.connexion3a36.services;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    public static final String QUIZ_TITLE_ALREADY_EXISTS_MESSAGE = "Un quiz avec ce titre existe deja.";
    public static final String QUIZ_TITLE_REQUIRED_MESSAGE = "Le titre du quiz est obligatoire.";
    public static final String QUIZ_TITLE_TOO_SHORT_MESSAGE = "Le titre doit contenir au moins 2 caracteres.";
    public static final String QUIZ_TITLE_INVALID_CHARACTERS_MESSAGE = "Le titre contient des caracteres non autorises.";

    private final Connection cnx;

    public QuizService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouterQuiz(Quiz quiz) throws SQLException {
        validateQuizTitle(quiz != null ? quiz.getTitre() : null);
        if (quizTitleExists(quiz.getTitre())) {
            throw new SQLException(QUIZ_TITLE_ALREADY_EXISTS_MESSAGE);
        }

        String requete = "INSERT INTO quiz (titre, statut, created_by, duree, level) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, quiz.getTitre().trim());
            pst.setString(2, quiz.getStatut());
            pst.setString(3, quiz.getCreatedBy());
            pst.setInt(4, quiz.getDuree());
            pst.setString(5, quiz.getLevel());
            pst.executeUpdate();
        }
    }

    public List<Quiz> afficherQuiz() throws SQLException {
        String requete = "SELECT id_quiz, titre, statut, created_by, duree, level FROM quiz";
        List<Quiz> quizzes = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setIdQuiz(rs.getInt("id_quiz"));
                quiz.setTitre(rs.getString("titre"));
                quiz.setStatut(rs.getString("statut"));
                quiz.setCreatedBy(rs.getString("created_by"));
                quiz.setDuree(rs.getInt("duree"));
                quiz.setLevel(rs.getString("level"));
                quizzes.add(quiz);
            }
        }

        return quizzes;
    }

    public List<Quiz> afficherQuizValides() throws SQLException {
        String requete = "SELECT id_quiz, titre, statut, created_by, duree, level FROM quiz WHERE LOWER(statut) IN (LOWER(?), LOWER(?))";
        List<Quiz> quizzes = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, "valide");
            pst.setString(2, "valide");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Quiz quiz = new Quiz();
                    quiz.setIdQuiz(rs.getInt("id_quiz"));
                    quiz.setTitre(rs.getString("titre"));
                    quiz.setStatut(rs.getString("statut"));
                    quiz.setCreatedBy(rs.getString("created_by"));
                    quiz.setDuree(rs.getInt("duree"));
                    quiz.setLevel(rs.getString("level"));
                    quizzes.add(quiz);
                }
            }
        }

        return quizzes;
    }

    public void modifierQuiz(Quiz quiz) throws SQLException {
        validateQuizTitle(quiz != null ? quiz.getTitre() : null);
        if (quizTitleExistsForAnotherQuiz(quiz.getTitre(), quiz.getIdQuiz())) {
            throw new SQLException(QUIZ_TITLE_ALREADY_EXISTS_MESSAGE);
        }

        String requete = "UPDATE quiz SET titre = ?, statut = ?, created_by = ?, duree = ?, level = ? WHERE id_quiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, quiz.getTitre().trim());
            pst.setString(2, quiz.getStatut());
            pst.setString(3, quiz.getCreatedBy());
            pst.setInt(4, quiz.getDuree());
            pst.setString(5, quiz.getLevel());
            pst.setInt(6, quiz.getIdQuiz());
            pst.executeUpdate();
        }
    }

    public boolean quizTitleExists(String titre) throws SQLException {
        return countQuizByTitle(titre, null) > 0;
    }

    public boolean quizTitleExistsForAnotherQuiz(String titre, int currentQuizId) throws SQLException {
        return countQuizByTitle(titre, currentQuizId) > 0;
    }

    public void supprimerQuiz(int id) throws SQLException {
        String requete = "DELETE FROM quiz WHERE id_quiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public void validerQuiz(int id) throws SQLException {
        updateQuizStatus(id, "valide", "validation");
    }

    public void rejeterQuiz(int id) throws SQLException {
        updateQuizStatus(id, "rejete", "rejet");
    }

    private void updateQuizStatus(int id, String statut, String action) throws SQLException {
        String requete = "UPDATE quiz SET statut = ? WHERE id_quiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, statut);
            pst.setInt(2, id);
            int updatedRows = pst.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Aucun quiz mis a jour lors du " + action + ".");
            }
        }
    }

    private int countQuizByTitle(String titre, Integer excludedQuizId) throws SQLException {
        StringBuilder requete = new StringBuilder(
                "SELECT COUNT(*) FROM quiz WHERE LOWER(TRIM(titre)) = LOWER(TRIM(?))"
        );
        if (excludedQuizId != null) {
            requete.append(" AND id_quiz <> ?");
        }

        try (PreparedStatement pst = cnx.prepareStatement(requete.toString())) {
            pst.setString(1, titre.trim());
            if (excludedQuizId != null) {
                pst.setInt(2, excludedQuizId);
            }
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    private void validateQuizTitle(String titre) throws SQLException {
        if (titre == null || titre.trim().isEmpty()) {
            throw new SQLException(QUIZ_TITLE_REQUIRED_MESSAGE);
        }

        String trimmedTitle = titre.trim();
        if (trimmedTitle.length() < 2) {
            throw new SQLException(QUIZ_TITLE_TOO_SHORT_MESSAGE);
        }
        if (!trimmedTitle.matches("[\\p{L}\\p{Nd} _-]+")) {
            throw new SQLException(QUIZ_TITLE_INVALID_CHARACTERS_MESSAGE);
        }
    }
}
