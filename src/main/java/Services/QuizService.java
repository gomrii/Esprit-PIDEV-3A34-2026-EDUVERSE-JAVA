package Services;

import Entities.Question;
import Entities.Quiz;
import Entities.Reponse;
import Utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    public static final String QUIZ_TITLE_ALREADY_EXISTS_MESSAGE = "Un quiz avec ce titre existe deja.";
    public static final String QUIZ_THEME_ALREADY_USED_MESSAGE = "Ce theme a deja ete utilise pour un quiz existant.";
    public static final String QUIZ_GENERATION_CANCELLED_DUPLICATE_MESSAGE = "La generation est annulee pour eviter un doublon.";
    public static final String QUIZ_DUPLICATE_QUESTIONS_MESSAGE =
            "Certaines questions generees existent deja dans la base. La generation est annulee pour eviter un doublon.";
    public static final String QUIZ_TITLE_REQUIRED_MESSAGE = "Le titre du quiz est obligatoire.";
    public static final String QUIZ_TITLE_TOO_SHORT_MESSAGE = "Le titre doit contenir au moins 2 caracteres.";
    public static final String QUIZ_TITLE_INVALID_CHARACTERS_MESSAGE = "Le titre contient des caracteres non autorises.";

    private final Connection cnx;

    public QuizService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public void ajouterQuiz(Quiz quiz) throws SQLException {
        validateQuizForPersistence(quiz);
        if (quizTitleExists(quiz.getTitre())) {
            throw new SQLException(QUIZ_TITLE_ALREADY_EXISTS_MESSAGE);
        }

        String requete = "INSERT INTO quiz (titre, statut, createdBy, duree, level) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, quiz.getTitre().trim());
            pst.setString(2, normalizeQuizStatus(quiz.getStatut()));
            pst.setString(3, normalizeCreatedBy(quiz.getCreatedBy()));
            pst.setInt(4, quiz.getDuree());
            pst.setString(5, normalizeLevel(quiz.getLevel()));
            pst.executeUpdate();
        }
    }

    public int ajouterQuizComplet(Quiz quiz) throws SQLException {
        ensureGeneratedQuizCanBeSaved(quiz);

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
        String requete = "SELECT idQuiz, titre, statut, createdBy, duree, level FROM quiz";
        List<Quiz> quizzes = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                quizzes.add(mapQuiz(rs));
            }
        }

        return quizzes;
    }

    public List<Quiz> afficherQuizValides() throws SQLException {
        String requete = "SELECT idQuiz, titre, statut, createdBy, duree, level FROM quiz WHERE LOWER(TRIM(statut)) = LOWER(TRIM(?))";
        List<Quiz> quizzes = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, "valide");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapQuiz(rs));
                }
            }
        }

        return quizzes;
    }

    public void modifierQuiz(Quiz quiz) throws SQLException {
        validateQuizForPersistence(quiz);
        if (quizTitleExistsForAnotherQuiz(quiz.getTitre(), quiz.getIdQuiz())) {
            throw new SQLException(QUIZ_TITLE_ALREADY_EXISTS_MESSAGE);
        }

        String requete = "UPDATE quiz SET titre = ?, statut = ?, createdBy = ?, duree = ?, level = ? WHERE idQuiz = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, quiz.getTitre().trim());
            pst.setString(2, normalizeQuizStatus(quiz.getStatut()));
            pst.setString(3, normalizeCreatedBy(quiz.getCreatedBy()));
            pst.setInt(4, quiz.getDuree());
            pst.setString(5, normalizeLevel(quiz.getLevel()));
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

    public void ensureQuizGenerationAllowed(String titre) throws SQLException {
        validateQuizTitle(titre);
        if (quizTitleExists(titre)) {
            throw new SQLException(QUIZ_TITLE_ALREADY_EXISTS_MESSAGE + " " + QUIZ_GENERATION_CANCELLED_DUPLICATE_MESSAGE);
        }
        if (quizThemeExists(titre)) {
            throw new SQLException(QUIZ_THEME_ALREADY_USED_MESSAGE + " " + QUIZ_GENERATION_CANCELLED_DUPLICATE_MESSAGE);
        }
    }

    public void ensureGeneratedQuizCanBeSaved(Quiz quiz) throws SQLException {
        validateQuizForGeneration(quiz);
        if (quizTitleExists(quiz.getTitre())) {
            throw new SQLException(QUIZ_TITLE_ALREADY_EXISTS_MESSAGE + " " + QUIZ_GENERATION_CANCELLED_DUPLICATE_MESSAGE);
        }
        if (quizThemeExists(quiz.getTitre())) {
            throw new SQLException(QUIZ_THEME_ALREADY_USED_MESSAGE + " " + QUIZ_GENERATION_CANCELLED_DUPLICATE_MESSAGE);
        }
        if (!findDuplicateQuestionTexts(quiz.getQuestions()).isEmpty()) {
            throw new SQLException(QUIZ_DUPLICATE_QUESTIONS_MESSAGE);
        }
    }

    public List<String> findExistingQuestionsForTheme(String titre, int limit) throws SQLException {
        List<String> questions = new ArrayList<>();
        String normalizedTitle = normalizeTitleForSearch(titre);
        String requete = """
                SELECT DISTINCT q.question
                FROM question q
                INNER JOIN quiz z ON z.idQuiz = q.idQuiz
                WHERE LOWER(TRIM(z.titre)) LIKE ?
                ORDER BY z.idQuiz DESC, q.idQuestion DESC
                LIMIT ?
                """;

        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, "%" + normalizedTitle + "%");
            pst.setInt(2, Math.max(limit, 1));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String question = rs.getString(1);
                    if (question != null && !question.isBlank()) {
                        questions.add(question.trim());
                    }
                }
            }
        }

        return questions;
    }

    public void supprimerQuiz(int id) throws SQLException {
        String requete = "DELETE FROM quiz WHERE idQuiz = ?";
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
        String requete = "UPDATE quiz SET statut = ? WHERE idQuiz = ?";
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
            requete.append(" AND idQuiz <> ?");
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

    private boolean quizThemeExists(String titre) throws SQLException {
        return countQuizByTheme(titre) > 0;
    }

    private int countQuizByTheme(String titre) throws SQLException {
        String normalizedTitle = normalizeTitleForSearch(titre);
        String requete = """
                SELECT COUNT(*)
                FROM quiz
                WHERE (
                        LOWER(TRIM(titre)) LIKE ?
                        OR ? LIKE CONCAT('%', LOWER(TRIM(titre)), '%')
                      )
                  AND LOWER(TRIM(titre)) <> LOWER(TRIM(?))
                """;

        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, "%" + normalizedTitle + "%");
            pst.setString(2, normalizedTitle);
            pst.setString(3, titre.trim());
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

    private void validateQuizForPersistence(Quiz quiz) throws SQLException {
        if (quiz == null) {
            throw new SQLException("Le quiz est introuvable.");
        }
        validateQuizTitle(quiz.getTitre());
        if (normalizeQuizStatus(quiz.getStatut()).isBlank()) {
            throw new SQLException("Le statut du quiz est obligatoire.");
        }
        if (normalizeCreatedBy(quiz.getCreatedBy()).isBlank()) {
            throw new SQLException("Le createur du quiz est obligatoire.");
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

    private List<String> findDuplicateQuestionTexts(List<Question> questions) throws SQLException {
        List<String> duplicates = new ArrayList<>();
        if (questions == null) {
            return duplicates;
        }

        String requete = "SELECT COUNT(*) FROM question WHERE LOWER(TRIM(question)) = LOWER(TRIM(?))";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            for (Question question : questions) {
                if (question == null || question.getQuestion() == null || question.getQuestion().isBlank()) {
                    continue;
                }
                pst.setString(1, question.getQuestion().trim());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        duplicates.add(question.getQuestion().trim());
                    }
                }
            }
        }

        return duplicates;
    }

    private String normalizeTitleForSearch(String titre) {
        return titre == null ? "" : titre.trim().toLowerCase();
    }

    private String normalizeQuizStatus(String statut) {
        if (statut == null) {
            return "";
        }
        String normalized = statut.trim().toLowerCase();
        return switch (normalized) {
            case "en attente" -> "en_attente";
            default -> normalized;
        };
    }

    private String normalizeCreatedBy(String createdBy) {
        return createdBy == null ? "" : createdBy.trim();
    }

    private String normalizeLevel(String level) {
        return level == null ? null : level.trim();
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setIdQuiz(rs.getInt("idQuiz"));
        quiz.setTitre(rs.getString("titre"));
        quiz.setStatut(rs.getString("statut"));
        quiz.setCreatedBy(rs.getString("createdBy"));
        quiz.setDuree(rs.getInt("duree"));
        quiz.setLevel(rs.getString("level"));
        return quiz;
    }

    private int insertQuizAndReturnId(Quiz quiz) throws SQLException {
        String requete = "INSERT INTO quiz (titre, statut, createdBy, duree, level) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, quiz.getTitre().trim());
            pst.setString(2, normalizeQuizStatus(quiz.getStatut()));
            pst.setString(3, normalizeCreatedBy(quiz.getCreatedBy()));
            pst.setInt(4, quiz.getDuree());
            pst.setString(5, normalizeLevel(quiz.getLevel()));
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
        String requete = "INSERT INTO question (question, idQuiz) VALUES (?, ?)";
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
        String requete = "INSERT INTO reponse (reponse, score, idQuestion) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, reponse.getReponse().trim());
            pst.setDouble(2, reponse.getScore());
            pst.setInt(3, questionId);
            pst.executeUpdate();
        }
    }
}
