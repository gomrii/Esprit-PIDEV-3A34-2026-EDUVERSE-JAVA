package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.TwoFactorService;
import com.elearning.service.UserService;
import com.elearning.util.CaptchaGenerator;
import com.elearning.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller de la page de connexion.
 *
 * Nouvelles fonctionnalités ajoutées :
 *   - CAPTCHA mathématique visuel (Canvas JavaFX)
 *   - Lien "Mot de passe oublié" → ForgotPasswordView
 *   - Gestion 2FA : si user.isTwoFactorEnabled() → TwoFactorView
 *   - Protection contre brute-force (via UserService → UserDAO)
 */
public class LoginController implements Initializable {

    // Champs existants
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    // Nouveaux champs CAPTCHA
    @FXML private Canvas    captchaCanvas;
    @FXML private javafx.scene.control.Slider captchaSlider;
    @FXML private Label     errCaptcha;
    @FXML private Label     captchaInstruction;

    private final UserService      userService  = new UserService();
    private final TwoFactorService twofaService = new TwoFactorService();
    private final CaptchaGenerator captchaGen   = new CaptchaGenerator();

    // -------------------------------------------------------
    // initialize() — appelé automatiquement après chargement FXML
    // -------------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Générer le CAPTCHA au chargement de la vue
        if (captchaCanvas != null) {
            captchaGen.genererEtDessiner(captchaCanvas);
            if (captchaInstruction != null) {
                captchaInstruction.setText(captchaGen.getReponseAttendueTexte());
            }
        }
        if (captchaSlider != null) {
            captchaSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                captchaGen.dessinerRotation(captchaCanvas, newVal.doubleValue());
            });
        }
        if (errCaptcha != null) { errCaptcha.setVisible(false); errCaptcha.setManaged(false); }
        if (errorLabel != null) { errorLabel.setVisible(false); errorLabel.setManaged(false); }
    }

    // -------------------------------------------------------
    // Bouton de connexion
    // -------------------------------------------------------

    @FXML
    private void handleLogin(ActionEvent event) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        clearErrors();

        // Vérifier le CAPTCHA d'abord
        if (!validerCaptcha()) return;

        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            User user = userService.authentifier(email, password);

            // Vérifier si la 2FA est activée
            if (user.isTwoFactorEnabled()) {
                // Envoyer le code OTP par email
                try {
                    twofaService.envoyerEtSauvegarder(user);
                } catch (Exception ex) {
                    System.err.println("⚠ Email 2FA impossible : " + ex.getMessage());
                    // En cas d'échec d'envoi email, continuer sans 2FA (démo)
                    SessionManager.getInstance().connecter(user);
                    naviguerVersDashboard(user, event);
                    return;
                }

                // Naviguer vers TwoFactorView sans connecter l'utilisateur
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/com/elearning/gui/TwoFactorView.fxml"));
                    Parent root = loader.load();
                    TwoFactorController ctrl = loader.getController();
                    ctrl.setUtilisateur(user);

                    Scene scene = ((Node) event.getSource()).getScene();
                    scene.setRoot(root);
                    scene.getStylesheets().clear();
                    scene.getStylesheets().add(
                            getClass().getResource("/com/elearning/css/style.css").toExternalForm());
                } catch (IOException ex) {
                    afficherErreur("Erreur lors du chargement de la vérification 2FA.");
                }

            } else {
                // Pas de 2FA : connexion directe
                SessionManager.getInstance().connecter(user);
                naviguerVersDashboard(user, event);
            }

        } catch (UserService.ValidationException e) {
            afficherErreur(e.getMessage());
            // Régénérer le CAPTCHA après erreur
            if (captchaCanvas != null) captchaGen.genererEtDessiner(captchaCanvas);
            if (captchaSlider  != null) captchaSlider.setValue(0);
        }
    }

    // -------------------------------------------------------
    // Face ID
    // -------------------------------------------------------

    @FXML
    private void handleFaceIdLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            afficherErreur("Veuillez saisir votre email pour utiliser Face ID.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/FaceIdSimulatorView.fxml"));
            Parent root = loader.load();
            FaceIdSimulatorController ctrl = loader.getController();

            Stage modal = new Stage();
            modal.setTitle("Simulation Face ID");
            modal.setScene(new Scene(root, 400, 500));
            modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modal.show();

            // Démarrer l'animation de scan en passant ce controller
            ctrl.demarrerScan(email, this);

        } catch (IOException e) {
            afficherErreur("Erreur lors de l'ouverture du simulateur Face ID.");
        }
    }

    public void validerFaceId(User user) {
        SessionManager.getInstance().connecter(user);

        // Routage selon le rôle — identique au login classique
        String fxmlPath = switch (user.getRole()) {
            case User.ROLE_ADMIN      -> "/com/elearning/gui/AdminDashboardView.fxml";
            case User.ROLE_ENSEIGNANT -> "/com/elearning/gui/EnseignantDashboardView.fxml";
            default                   -> "/com/elearning/gui/EtudiantDashboardView.fxml";
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = loginButton.getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
        } catch (IOException e) {
            afficherErreur("Erreur de navigation vers le tableau de bord.");
        }
    }

    // -------------------------------------------------------
    // CAPTCHA
    // -------------------------------------------------------

    @FXML
    private void handleRefreshCaptcha(ActionEvent event) {
        if (captchaCanvas != null) captchaGen.genererEtDessiner(captchaCanvas);
        if (captchaSlider != null) captchaSlider.setValue(0);
        if (captchaInstruction != null) captchaInstruction.setText(captchaGen.getReponseAttendueTexte());
        if (errCaptcha    != null) { errCaptcha.setVisible(false); errCaptcha.setManaged(false); }
    }

    private boolean validerCaptcha() {
        // Si le canvas n'est pas dans le FXML, on skip la vérification
        if (captchaCanvas == null || captchaSlider == null) return true;

        if (!captchaGen.verifier(String.valueOf(captchaSlider.getValue()))) {
            if (errCaptcha != null) {
                errCaptcha.setText("❌ Veuillez redresser l'image correctement.");
                errCaptcha.setVisible(true);
                errCaptcha.setManaged(true);
            }
            captchaGen.genererEtDessiner(captchaCanvas);
            captchaSlider.setValue(0);
            if (captchaInstruction != null) captchaInstruction.setText(captchaGen.getReponseAttendueTexte());
            return false;
        }
        if (errCaptcha != null) { errCaptcha.setVisible(false); errCaptcha.setManaged(false); }
        return true;
    }

    // -------------------------------------------------------
    // Mot de passe oublié
    // -------------------------------------------------------

    @FXML
    private void handleMotDePasseOublie(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/ForgotPasswordView.fxml"));
            Parent root = loader.load();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de la page.");
        }
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/RegisterView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            stage.setTitle("Eduverse — Inscription");
        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de l'interface.");
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------

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
