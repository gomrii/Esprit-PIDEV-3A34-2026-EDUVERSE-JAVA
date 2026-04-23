package edu.connexion3a36.services;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public int ajouterQuizComplet(Quiz quiz) throws SQLException {
        validateQuizForGeneration(quiz);
        if (quizTitleExists(quiz.getTitre())) {
            throw new SQLException(QUIZ_TITLE_ALREADY_EXISTS_MESSAGE);
        }

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            int quizId = insertQuizAndReturnId(quiz);
            quiz.setIdQuiz(quizId);

            for (Question question : quiz.getQuestions()) {
                int questionId = insertQuestionAndReturnId(question, quizId);
                question.setIdQuestion(questionId);
                question.setIdQuiz(quizId);

                for (Reponse reponse : question.getReponses()) {
                    insertAnswer(reponse, questionId);
                    reponse.setIdQuestion(questionId);
                }
            }

            cnx.commit();
            return quizId;
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(autoCommit);
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

    private void validateQuizForGeneration(Quiz quiz) throws SQLException {
        validateQuizTitle(quiz != null ? quiz.getTitre() : null);
        if (quiz == null) {
            throw new SQLException("Le quiz est introuvable.");
        }
        if (quiz.getDuree() <= 0) {
            throw new SQLException("La duree doit etre un entier positif.");
        }
        if (quiz.getLevel() == null || quiz.getLevel().isBlank()) {
            throw new SQLException("Le niveau du quiz est obligatoire.");
        }
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new SQLException("Ajoutez au moins une question avant l enregistrement.");
        }

        for (Question question : quiz.getQuestions()) {
            if (question == null || question.getQuestion() == null || question.getQuestion().trim().length() < 2) {
                throw new SQLException("Chaque question doit contenir au moins 2 caracteres.");
            }
            if (question.getReponses() == null || question.getReponses().isEmpty()) {
                throw new SQLException("Chaque question doit contenir au moins une reponse.");
            }
            for (Reponse reponse : question.getReponses()) {
                if (reponse == null || reponse.getReponse() == null || reponse.getReponse().trim().length() < 1) {
                    throw new SQLException("Chaque reponse doit etre renseignee.");
                }
            }
        }
    }

    private int insertQuizAndReturnId(Quiz quiz) throws SQLException {
        String requete = "INSERT INTO quiz (titre, statut, created_by, duree, level) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, quiz.getTitre().trim());
            pst.setString(2, quiz.getStatut());
            pst.setString(3, quiz.getCreatedBy());
            pst.setInt(4, quiz.getDuree());
            pst.setString(5, quiz.getLevel());
            pst.executeUpdate();

            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Impossible de recuperer l identifiant du quiz cree.");
    }

    private int insertQuestionAndReturnId(Question question, int quizId) throws SQLException {
        String requete = "INSERT INTO question (question, id_quiz) VALUES (?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, question.getQuestion().trim());
            pst.setInt(2, quizId);
            pst.executeUpdate();

            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Impossible de recuperer l identifiant de la question creee.");
    }

    private void insertAnswer(Reponse reponse, int questionId) throws SQLException {
        String requete = "INSERT INTO reponse (reponse, score, id_question) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, reponse.getReponse().trim());
            pst.setDouble(2, reponse.getScore());
            pst.setInt(3, questionId);
            pst.executeUpdate();
        }
    }
}
