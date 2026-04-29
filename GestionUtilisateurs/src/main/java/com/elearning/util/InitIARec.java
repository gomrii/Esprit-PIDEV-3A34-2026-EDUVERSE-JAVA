package com.elearning.util;

import java.sql.Connection;
import java.sql.Statement;

public class InitIARec {
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS ai_recommendation (" +
                     "    id              INT          NOT NULL AUTO_INCREMENT," +
                     "    priorite        VARCHAR(10)  NOT NULL," +
                     "    type            VARCHAR(20)  NOT NULL," +
                     "    titre           VARCHAR(255) NOT NULL," +
                     "    description     TEXT         NOT NULL," +
                     "    action_suggeree TEXT         NOT NULL," +
                     "    lue             TINYINT(1)   NOT NULL DEFAULT 0," +
                     "    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "    PRIMARY KEY (id)" +
                     ");";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Table ai_recommendation créée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
