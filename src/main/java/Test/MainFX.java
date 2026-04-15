package Test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML depuis les resources
        Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("EduVerse - Gestion des Événements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
