package edu.connexion3a36.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
        Parent root = loader.load();
        Scene sc = new Scene(root, 1100, 700);
        // apply custom stylesheet
        sc.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("Gestion des quiz");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(sc);
        primaryStage.show();
    }
}