package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainMenu {

    @FXML
    void handleAdmin(ActionEvent event) {
        // Redirection vers AffichierCoursAdmin.fxml (ton fichier de la capture)
        navigate(event, "/AffichierCoursAdmin.fxml", "Espace Administrateur");
    }

    @FXML
    void handleEnseignant(ActionEvent event) {
        // Redirection vers AffichierCoursEnseignant.fxml
        navigate(event, "/AffichierCoursEnseignant.fxml", "Espace Enseignant");
    }

    @FXML
    void handleStudent(ActionEvent event) {
        // Redirection vers AffichierCoursStudent.fxml
        navigate(event, "/AffichierCoursStudent.fxml", "Espace Étudiant");
    }

    private void navigate(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement du FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }
}