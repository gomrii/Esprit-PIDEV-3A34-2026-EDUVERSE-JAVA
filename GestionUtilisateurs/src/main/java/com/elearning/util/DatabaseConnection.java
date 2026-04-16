package com.elearning.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connexion à la base de données — Pattern Singleton.
 *
 * Pourquoi Singleton ?
 *   Ouvrir une connexion MySQL est coûteux (environ 50-100 ms).
 *   On ne veut qu'UNE seule connexion partagée dans toute l'application.
 *   Le Singleton garantit qu'une seule instance de la connexion existe.
 *
 * géré par le conteneur d'injection de dépendances.
 */
public class DatabaseConnection {

    // -------------------------------------------------------
    // Paramètres de connexion — à adapter selon votre config
    // -------------------------------------------------------
    private static final String URL      = "jdbc:mysql://localhost:3307/eduverse-java?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";           // Mettez votre mot de passe MySQL ici

    /** Instance unique (volatile pour la thread-safety) */
    private static volatile DatabaseConnection instance;

    /** La connexion JDBC réelle */
    private Connection connection;

    // -------------------------------------------------------
    // Constructeur PRIVÉ → personne ne peut faire "new DatabaseConnection()"
    // -------------------------------------------------------
    private DatabaseConnection() {
        try {
            // Charger le driver MySQL (inutile avec Java 9+ + Service Loader, mais explicite)
            Class.forName("com.mysql.cj.jdbc.Driver");

            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion MySQL établie.");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL introuvable. Ajoutez mysql-connector-j.jar au classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de se connecter à MySQL : " + e.getMessage(), e);
        }
    }

    /**
     * Point d'accès unique à l'instance.
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
     * Si elle est fermée (ex: timeout MySQL), en ouvre une nouvelle.
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
                System.out.println("🔌 Connexion MySQL fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture connexion : " + e.getMessage());
        }
    }
}

