package com.elearning;

import com.elearning.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application JavaFX.
 *
 * Équivalent Symfony : public/index.php (le front controller).
 *
 * JavaFX utilise un pattern similaire à un framework MVC :
 * - Application.start() = bootstrap
 * - FXML = les templates Twig (vue)
 * - Controller = controller Symfony
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la vue de login (première page affichée)
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/elearning/gui/LoginView.fxml"));

        Scene scene = new Scene(loader.load(), 500, 400);

        // Appliquer le CSS global
        scene.getStylesheets().add(
                getClass().getResource("/com/elearning/css/style.css").toExternalForm());

        primaryStage.setTitle("Eduverse — Gestion des Utilisateurs");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Appelé à la fermeture de l'application.
     * Fermer proprement la connexion MySQL.
     */
    @Override
    public void stop() throws Exception {
        DatabaseConnection.getInstance().close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
