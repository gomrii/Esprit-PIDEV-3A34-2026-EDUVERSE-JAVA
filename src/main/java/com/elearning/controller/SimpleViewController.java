package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller partagé pour les vues Enseignant et Étudiant.
 * Affiche un message de bienvenue et gère la déconnexion.
 */
public class SimpleViewController implements Initializable {

    @FXML private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + user.getFullName() + " !");
        }
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        SessionManager.getInstance().deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 500, 400);
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(500);
            stage.setHeight(400);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
