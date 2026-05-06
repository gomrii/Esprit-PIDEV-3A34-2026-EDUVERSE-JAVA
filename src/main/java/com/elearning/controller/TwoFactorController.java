package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.TwoFactorService;
import com.elearning.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller de l'écran de vérification en deux étapes (2FA OTP).
 *
 * Reçoit l'utilisateur en attente de validation via setUtilisateur().
 * Démarrage automatique du compte à rebours (5 min).
 * Limite à 3 essais avant retour au login.
 */
public class TwoFactorController implements Initializable {

    @FXML private Label        labelEmailMasque;
    @FXML private TextField    codeField;
    @FXML private Label        labelTimer;
    @FXML private Label        errLabel;
    @FXML private Button       btnValider;
    @FXML private Button       btnRenvoyer;
    @FXML private ProgressIndicator loadingIndicator;

    private final TwoFactorService twofaService = new TwoFactorService();

    private User      utilisateurEnAttente;   // pas encore en session
    private Timeline  compteARebours;
    private int       secondesRestantes = 300; // 5 minutes
    private int       essaisRestants    = 3;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Accepter uniquement les chiffres dans le champ code
        codeField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*")) {
                codeField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (newVal.length() > 6) {
                codeField.setText(newVal.substring(0, 6));
            }
        });

        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (errLabel != null) { errLabel.setVisible(false); errLabel.setManaged(false); }
    }

    /**
     * Injecte l'utilisateur en attente de validation 2FA.
     * Appelé par LoginController après authentification réussie.
     */
    public void setUtilisateur(User user) {
        this.utilisateurEnAttente = user;

        // Afficher email masqué : "ah***@gmail.com"
        if (labelEmailMasque != null) {
            labelEmailMasque.setText(masquerEmail(user.getEmail()));
        }

        // Démarrer le compte à rebours
        demarrerTimer();
    }

    // -------------------------------------------------------
    // Timer
    // -------------------------------------------------------

    private void demarrerTimer() {
        secondesRestantes = 300;
        if (compteARebours != null) compteARebours.stop();

        compteARebours = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondesRestantes--;
            int min = secondesRestantes / 60;
            int sec = secondesRestantes % 60;
            if (labelTimer != null) {
                labelTimer.setText(String.format("⏱ Code valide pendant %d:%02d", min, sec));
            }
            if (secondesRestantes <= 0) {
                compteARebours.stop();
                if (labelTimer != null) labelTimer.setText("⌛ Code expiré. Cliquez sur Renvoyer.");
                if (btnValider != null) btnValider.setDisable(true);
            }
        }));
        compteARebours.setCycleCount(Timeline.INDEFINITE);
        compteARebours.play();
    }

    // -------------------------------------------------------
    // Validation du code
    // -------------------------------------------------------

    @FXML
    private void handleValider(ActionEvent event) {
        String code = codeField.getText().trim();
        if (code.isBlank()) {
            afficherErreur("Veuillez saisir le code reçu par email.");
            return;
        }

        if (twofaService.verifierCode(utilisateurEnAttente.getId(), code)) {
            // Code correct : connecter l'utilisateur
            if (compteARebours != null) compteARebours.stop();
            SessionManager.getInstance().connecter(utilisateurEnAttente);
            naviguerVersDashboard(utilisateurEnAttente, event);
        } else {
            essaisRestants--;
            codeField.clear();
            if (essaisRestants <= 0) {
                afficherErreur("🔒 Trop de tentatives incorrectes. Retour à la connexion.");
                if (compteARebours != null) compteARebours.stop();
                naviguerVersLogin(event);
            } else {
                afficherErreur("❌ Code incorrect. " + essaisRestants + " essai(s) restant(s).");
            }
        }
    }

    // -------------------------------------------------------
    // Renvoyer le code
    // -------------------------------------------------------

    @FXML
    private void handleRenvoyer(ActionEvent event) {
        essaisRestants = 3;
        btnRenvoyer.setDisable(true);
        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                twofaService.envoyerEtSauvegarder(utilisateurEnAttente);
                Platform.runLater(() -> {
                    btnRenvoyer.setDisable(false);
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    if (btnValider != null) btnValider.setDisable(false);
                    demarrerTimer();
                    afficherErreur("✅ Nouveau code envoyé à votre email !");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    btnRenvoyer.setDisable(false);
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    afficherErreur("⚠ Impossible d'envoyer l'email : " + ex.getMessage());
                });
            }
        }).start();
    }

    // -------------------------------------------------------
    // Navigation
    // -------------------------------------------------------

    private void naviguerVersDashboard(User user, ActionEvent event) {
        String fxmlPath = switch (user.getRole()) {
            case User.ROLE_ADMIN      -> "/com/elearning/gui/AdminDashboardView.fxml";
            case User.ROLE_ENSEIGNANT -> "/com/elearning/gui/EnseignantDashboardView.fxml";
            default                   -> "/com/elearning/gui/EtudiantDashboardView.fxml";
        };
        naviguerVers(fxmlPath, event);
    }

    private void naviguerVersLogin(ActionEvent event) {
        naviguerVers("/com/elearning/gui/LoginView.fxml", event);
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
            afficherErreur("Erreur de navigation : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------

    private String masquerEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int at = email.indexOf('@');
        if (at <= 2) return "***" + email.substring(at);
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    private void afficherErreur(String msg) {
        if (errLabel != null) {
            errLabel.setText(msg);
            errLabel.setVisible(true);
            errLabel.setManaged(true);
        }
    }
}
