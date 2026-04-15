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
        Parent root = loader.load();
        
        // Forcer les dimensions pour éviter la déformation
        // (Comme demandé dans "Problème 1")
        root.setPrefWidth(900);
        root.setPrefHeight(600);

        Scene scene = new Scene(root);

        // Appliquer le CSS global
        scene.getStylesheets().add(
                getClass().getResource("/com/elearning/css/style.css").toExternalForm());

        primaryStage.setTitle("Eduverse — Gestion des Utilisateurs");
        primaryStage.setScene(scene);
        
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
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
