package Utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class QuizSchemaInitializer {

    private QuizSchemaInitializer() {
    }

    public static void ensureSchema(Connection connection) throws SQLException {
        if (connection == null) {
            throw new SQLException("Connexion SQL indisponible pour l'initialisation du schema quiz.");
        }

        createTablesIfMissing(connection);
        renameLegacyColumns(connection);
    }

    private static void createTablesIfMissing(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS quiz (
                        idQuiz INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        titre VARCHAR(255) NOT NULL,
                        statut VARCHAR(100) NOT NULL,
                        createdBy VARCHAR(255) NOT NULL,
                        duree INT NOT NULL DEFAULT 0,
                        level VARCHAR(100) NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS question (
                        idQuestion INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        question TEXT NOT NULL,
                        idQuiz INT NOT NULL,
                        CONSTRAINT fk_question_quiz
                            FOREIGN KEY (idQuiz) REFERENCES quiz(idQuiz)
                            ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS reponse (
                        idReponse INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        reponse TEXT NOT NULL,
                        score DOUBLE NOT NULL DEFAULT 0,
                        idQuestion INT NOT NULL,
                        CONSTRAINT fk_reponse_question
                            FOREIGN KEY (idQuestion) REFERENCES question(idQuestion)
                            ON DELETE CASCADE
                    )
                    """);
        }
    }

    private static void renameLegacyColumns(Connection connection) throws SQLException {
        renameColumnIfNeeded(connection, "quiz", "id_quiz", "idQuiz");
        renameColumnIfNeeded(connection, "quiz", "created_by", "createdBy");

        renameColumnIfNeeded(connection, "question", "id_question", "idQuestion");
        renameColumnIfNeeded(connection, "question", "id_quiz", "idQuiz");
        renameColumnIfNeeded(connection, "question", "quiz_id", "idQuiz");

        renameColumnIfNeeded(connection, "reponse", "id_reponse", "idReponse");
        renameColumnIfNeeded(connection, "reponse", "id_question", "idQuestion");
        renameColumnIfNeeded(connection, "reponse", "question_id", "idQuestion");
        renameColumnIfNeeded(connection, "reponse", "reponse_id", "idReponse");
    }

    private static void renameColumnIfNeeded(Connection connection, String tableName, String oldName, String newName) throws SQLException {
        if (!columnExists(connection, tableName, oldName) || columnExists(connection, tableName, newName)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tableName + " RENAME COLUMN " + oldName + " TO " + newName);
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return resultSet.next();
        }
    }
}
