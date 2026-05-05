package com.elearning.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connexion Ã  la base de donnÃ©es â€” Pattern Singleton.
 *
 * Pourquoi Singleton ?
 *   Ouvrir une connexion MySQL est coÃ»teux (environ 50-100 ms).
 *   On ne veut qu'UNE seule connexion partagÃ©e dans toute l'application.
 *   Le Singleton garantit qu'une seule instance de la connexion existe.
 *
 * gÃ©rÃ© par le conteneur d'injection de dÃ©pendances.
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/eduverse?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    
    private static final String USER     = "root";
    private static final String PASSWORD = "";           // Mettez votre mot de passe MySQL ici

    /** Instance unique (volatile pour la thread-safety) */
    private static volatile DatabaseConnection instance;

    /** La connexion JDBC rÃ©elle */
    private Connection connection;

    // -------------------------------------------------------
    // Constructeur PRIVÃ‰ â†’ personne ne peut faire "new DatabaseConnection()"
    // -------------------------------------------------------
    private DatabaseConnection() {
        try {
            // Charger le driver MySQL (inutile avec Java 9+ + Service Loader, mais explicite)
            Class.forName("com.mysql.cj.jdbc.Driver");

            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("âœ… Connexion MySQL Ã©tablie.");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL introuvable. Ajoutez mysql-connector-j.jar au classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de se connecter Ã  MySQL : " + e.getMessage(), e);
        }
    }

    /**
     * Point d'accÃ¨s unique Ã  l'instance.
     * Double-checked locking pour la thread-safety.
     *
     * Usage :
     *   Connection conn = DatabaseConnection.getInstance().getConnection();
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Retourne la connexion JDBC.
     * Si elle est fermÃ©e (ex: timeout MySQL), en ouvre une nouvelle.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la reconnexion : " + e.getMessage(), e);
        }
        return connection;
    }

    /** Ferme proprement la connexion (appeler au shutdown de l'app) */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("ðŸ”Œ Connexion MySQL fermÃ©e.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture connexion : " + e.getMessage());
        }
    }
}

