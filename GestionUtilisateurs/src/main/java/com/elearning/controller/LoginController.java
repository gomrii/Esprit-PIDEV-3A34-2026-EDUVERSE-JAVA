package com.elearning.controller;

import com.elearning.MainApp;  // ← AJOUTER CETTE LIGNE
import com.elearning.entity.User;
import com.elearning.service.UserService;
import com.elearning.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin(ActionEvent event) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        clearErrors();

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            User user = userService.authentifier(email, password);
            SessionManager.getInstance().connecter(user);

            String fxmlPath = switch (user.getRole()) {
                case User.ROLE_ADMIN -> "/com/elearning/gui/AdminDashboardView.fxml";
                case User.ROLE_ENSEIGNANT -> "/com/elearning/gui/EnseignantDashboardView.fxml";
                default -> "/com/elearning/gui/EtudiantDashboardView.fxml";
            };

            naviguerVers(fxmlPath, event);

        } catch (UserService.ValidationException e) {
            afficherErreur(e.getMessage());
        }
    }

    private void naviguerVers(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());

        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de l'interface.");
            e.printStackTrace();
        }
    }

    @FXML
    private void allerVersInscription(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/RegisterView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            stage.setTitle("Eduverse — Inscription");

        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de l'interface.");
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
        passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
    }

    private void clearErrors() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        emailField.setStyle("");
        passwordField.setStyle("");
    }
}
