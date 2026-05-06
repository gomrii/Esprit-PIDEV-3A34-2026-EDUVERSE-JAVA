package Services;

import Utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatbotDatabaseService {
    private static final String VALID_STATUS_CONDITION = "LOWER(TRIM(statut)) IN ('valide', 'valid\u00e9', 'valid')";
    private static final String PENDING_STATUS_CONDITION = "LOWER(TRIM(statut)) IN ('en_attente', 'en attente', 'attente', 'pending')";
    private static final String REJECTED_STATUS_CONDITION = "LOWER(TRIM(statut)) IN ('rejete', 'rejet\u00e9', 'rejected')";
    private static final String ADMIN_CREATED_BY_CONDITION = "LOWER(TRIM(createdBy)) = 'admin' OR UPPER(TRIM(createdBy)) LIKE 'ADMIN#%'";
    private static final String TEACHER_CREATED_BY_CONDITION = "LOWER(TRIM(createdBy)) IN ('teacher', 'enseignant') OR UPPER(TRIM(createdBy)) LIKE 'ENSEIGNANT#%'";

    private final Connection cnx;

    public ChatbotDatabaseService() {
        this.cnx = MyConnection.getInstance().getCnx();
    }

    public int countQuiz() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM quiz");
    }

    public int countQuestions() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM question");
    }

    public int countReponses() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM reponse");
    }

    public int countValidatedQuiz() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM quiz WHERE " + VALID_STATUS_CONDITION);
    }

    public int countPendingQuiz() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM quiz WHERE " + PENDING_STATUS_CONDITION);
    }

    public int countRejectedQuiz() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM quiz WHERE " + REJECTED_STATUS_CONDITION);
    }

    public int countQuizCreatedByAdmin() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM quiz WHERE " + ADMIN_CREATED_BY_CONDITION);
    }

    public int countQuizCreatedByTeacher() throws SQLException {
        return executeCountQuery("SELECT COUNT(*) FROM quiz WHERE " + TEACHER_CREATED_BY_CONDITION);
    }

    private int executeCountQuery(String query) throws SQLException {
        if (cnx == null) {
            throw new SQLException("La connexion a la base de donnees est indisponible.");
        }

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }
}
