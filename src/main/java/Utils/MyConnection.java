package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/eduverse?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String LOGIN = "root";
    private static final String PASSWORD = "";

    private Connection cnx;

    public static MyConnection instance;

    private MyConnection() {
        cnx = createConnection();
    }

    public synchronized Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
                cnx = createConnection();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de verifier la connexion eduverse : " + e.getMessage(), e);
        }
        return cnx;
    }

    public static synchronized MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    private Connection createConnection() {
        try {
            Properties properties = new Properties();
            properties.setProperty("user", LOGIN);
            properties.setProperty("password", PASSWORD);
            Connection connection = DriverManager.getConnection(URL, properties);
            QuizSchemaInitializer.ensureSchema(connection);
            System.out.println("Connexion eduverse etablie.");
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Connexion a la base eduverse impossible : " + e.getMessage(), e);
        }
    }
}
