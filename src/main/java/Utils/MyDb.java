package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        } catch (SQLException e) {
            System.err.println("ERREUR DE CONNEXION BDD : " + e.getMessage());
            // On laisse conn à null, les services devront le gérer.
        }
    }

    public static MyDb getInstance() {
        if (instance == null) {
            instance = new MyDb();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
}
