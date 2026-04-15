package edu.connexion3a36.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.IOException;

public class TeacherHomeController {

    @FXML
    private void openQuizList(ActionEvent event) {
        open(event, "/teacher_quiz_list.fxml");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        open(event, "/home.fxml");
    }

    private void open(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la page: " + e.getMessage());
        }
    }
}