package com.elearning;

import com.elearning.util.DatabaseConnection;
import com.elearning.util.WindowHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        // Configuration initiale de la fenêtre
        primaryStage.setTitle("Eduverse — e-Learning");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        // Charger la première vue
        loadScene("/com/elearning/gui/LoginView.fxml");

        // Appliquer les dimensions et afficher
        WindowHelper.fixDimensions(primaryStage);
        primaryStage.show();
    }

    public void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());

            primaryStage.setScene(scene);

            // Forcer les dimensions à chaque changement de scène
            WindowHelper.fixDimensions(primaryStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
