package com.elearning.controller;

import com.elearning.service.PasswordResetService;
import com.elearning.service.UserService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller de la page "Mot de passe oublié".
 * L'utilisateur saisit son email, un code est envoyé par email (Mailtrap).
 * Après envoi réussi, navigation vers ResetPasswordView.
 */
public class ForgotPasswordController implements Initializable {

    @FXML private TextField         emailField;
    @FXML private Label             errEmail;
    @FXML private Button            btnEnvoyer;
    @FXML private ProgressIndicator loading;

    private final PasswordResetService resetService = new PasswordResetService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (errEmail != null) { errEmail.setVisible(false); errEmail.setManaged(false); }
        if (loading  != null)   loading.setVisible(false);
    }

    // -------------------------------------------------------
    // Envoi du code
    // -------------------------------------------------------

    @FXML
    private void handleEnvoyer(ActionEvent event) {
        // Masquer erreur précédente
        errEmail.setVisible(false);
        errEmail.setManaged(false);

        String email = emailField.getText().trim();
        if (email.isBlank()) {
            afficherErreur("L'adresse email est obligatoire.");
            return;
        }

        // Désactiver le bouton + afficher loader pendant l'envoi SMTP
        loading.setVisible(true);
        btnEnvoyer.setDisable(true);

        // Appel dans un Task pour ne pas bloquer le thread JavaFX
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                resetService.demanderReinitialisation(email);
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            loading.setVisible(false);
            btnEnvoyer.setDisable(false);
            // Naviguer vers l'écran de saisie du code
            naviguerVersReset(email, event);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            loading.setVisible(false);
            btnEnvoyer.setDisable(false);
            String msg = task.getException().getMessage();
            afficherErreur(msg != null ? msg : "Erreur lors de l'envoi. Réessayez.");
        }));

        new Thread(task).start();
    }

    // -------------------------------------------------------
    // Navigation
    // -------------------------------------------------------

    private void naviguerVersReset(String email, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/ResetPasswordView.fxml"));
            Parent root = loader.load();

            // Passer l'email au controller suivant
            ResetPasswordController ctrl = loader.getController();
            ctrl.setEmail(email);

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
        } catch (IOException e) {
            afficherErreur("Erreur de navigation : " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
        } catch (IOException e) {
            afficherErreur("Erreur de navigation.");
        }
    }

    // -------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------

    private void afficherErreur(String msg) {
        errEmail.setText("⚠ " + msg);
        errEmail.setVisible(true);
        errEmail.setManaged(true);
    }
}
