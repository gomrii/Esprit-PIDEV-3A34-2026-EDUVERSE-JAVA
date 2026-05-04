package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDb {
    public String PATH = "jdbc:mysql://localhost:3306/eduverse";
    public String user = "root";
    public String pwd = "";
    public Connection conn;
    public static MyDb instance;

    private MyDb() {
        try {
            conn = DriverManager.getConnection(PATH, user, pwd);
            System.out.println("Connexion à eduverse établie !");
            initTables(); // Correction automatique des tables manquantes
        } catch (SQLException e) {
            System.err.println("ERREUR DE CONNEXION BDD : " + e.getMessage());
        }
    }

    private void initTables() {
        try (Statement stmt = conn.createStatement()) {
            // Table Users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL)");
            
            // Insertion des utilisateurs par défaut si vide
            stmt.execute("INSERT IGNORE INTO users (id, username, role) VALUES " +
                    "(1, 'Admin', 'ADMIN'), " +
                    "(2, 'Etudiant', 'ETUDIANT'), " +
                    "(3, 'Enseignant', 'ENSEIGNANT')");

            // Table Club Membership
            stmt.execute("CREATE TABLE IF NOT EXISTS club_membership (" +
                    "user_id INT, " +
                    "club_id INT, " +
                    "status VARCHAR(50) DEFAULT 'PENDING', " +
                    "joined_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (user_id, club_id))");
            
            System.out.println("Correction effectuée : Les tables users et club_membership sont prêtes.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la correction des tables : " + e.getMessage());
        }
    }

    public static MyDb getInstance() {
        if (instance == null) {
            instance = new MyDb();
        }
        return instance;
    }

    public Connection getConn() {
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(1)) {
                System.out.println("Reconnexion à la base de données...");
                conn = DriverManager.getConnection(PATH, user, pwd);
            }
        } catch (SQLException e) {
            System.err.println("ERREUR DE RECONNEXION BDD : " + e.getMessage());
        }
        return conn;
    }
}
