package com.elearning.controller;

import com.elearning.service.PasswordResetService;
import com.elearning.util.PasswordGenerator;
import com.elearning.util.PasswordStrengthChecker;
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
 * Controller de la page de réinitialisation de mot de passe.
 *
 * L'utilisateur saisit le token reçu par email + le nouveau mot de passe.
 * Inclut un indicateur de force du mot de passe en temps réel.
 */
public class ResetPasswordController implements Initializable {

    @FXML private Label         labelEmail;
    @FXML private TextField     tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressBar   passwordStrengthBar;
    @FXML private Label         passwordStrengthLabel;
    @FXML private Label         errGlobal;
    @FXML private Button        btnReinitialiser;

    private final PasswordResetService resetService = new PasswordResetService();
    private String emailUtilisateur;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (errGlobal != null) { errGlobal.setVisible(false); errGlobal.setManaged(false); }
        if (passwordStrengthBar   != null) passwordStrengthBar.setProgress(0);
        if (passwordStrengthLabel != null) passwordStrengthLabel.setText("");

        // Indicateur de force du mot de passe en temps réel
        if (newPasswordField != null) {
            newPasswordField.textProperty().addListener((obs, old, nouveau) -> {
                PasswordStrengthChecker.PasswordStrengthResult result =
                        PasswordStrengthChecker.analyser(nouveau);

                if (passwordStrengthBar != null) {
                    passwordStrengthBar.setProgress(result.getProgress());
                    passwordStrengthBar.setStyle("-fx-accent: " + result.getCouleur() + ";");
                }
                if (passwordStrengthLabel != null) {
                    passwordStrengthLabel.setText(result.getLibelle());
                    passwordStrengthLabel.setStyle("-fx-text-fill: " + result.getCouleur() + ";");
                }

                // Bloquer si trop faible
                if (btnReinitialiser != null) {
                    btnReinitialiser.setDisable(result.getScore() < 3);
                }
            });
        }
    }

    /**
     * Injecte l'email de l'utilisateur (passé par ForgotPasswordController).
     */
    public void setEmail(String email) {
        this.emailUtilisateur = email;
        if (labelEmail != null) {
            labelEmail.setText("Code envoyé à : " + email);
        }
    }

    // -------------------------------------------------------
    // Réinitialisation
    // -------------------------------------------------------

    @FXML
    private void handleGenererMotDePasse(ActionEvent event) {
        String motDePasseGenere = PasswordGenerator.genererMotDePasseFort();
        newPasswordField.setText(motDePasseGenere);
        confirmPasswordField.setText(motDePasseGenere);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe généré");
        alert.setHeaderText(null);
        alert.setContentText("Voici le nouveau mot de passe (copiez-le) : \n\n" + motDePasseGenere);
        alert.showAndWait();
    }

    @FXML
    private void handleReinitialiser(ActionEvent event) {
        errGlobal.setVisible(false);
        errGlobal.setManaged(false);

        String token   = tokenField != null ? tokenField.getText().trim() : "";
        String nouveau = newPasswordField != null ? newPasswordField.getText() : "";
        String confirm = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        try {
            resetService.reinitialiserMotDePasse(token, nouveau, confirm);

            // Succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("✅ Mot de passe réinitialisé avec succès !\nVous pouvez maintenant vous connecter.");
            alert.showAndWait();

            naviguerVersLogin(event);

        } catch (Exception e) {
            afficherErreur(e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Navigation
    // -------------------------------------------------------

    @FXML
    private void handleRetour(ActionEvent event) {
        naviguerVersLogin(event);
    }

    private void naviguerVersLogin(ActionEvent event) {
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
        errGlobal.setText("⚠ " + (msg != null ? msg : "Erreur inconnue."));
        errGlobal.setVisible(true);
        errGlobal.setManaged(true);
    }
}
