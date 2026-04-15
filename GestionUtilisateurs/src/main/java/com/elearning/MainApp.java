package com.elearning;

import com.elearning.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application JavaFX.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/elearning/gui/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Appliquer le CSS global
        scene.getStylesheets().add(
                getClass().getResource("/com/elearning/css/style.css").toExternalForm());

        primaryStage.setTitle("Eduverse — Gestion des Utilisateurs");
        primaryStage.setScene(scene);

        // Les dimensions sont déjà définies dans chaque FXML (prefWidth/prefHeight)
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DatabaseConnection.getInstance().close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}