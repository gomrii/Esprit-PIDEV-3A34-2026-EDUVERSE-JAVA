package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.EmailValidationService;
import com.elearning.service.UserService;
import com.elearning.util.CaptchaGenerator;
import com.elearning.util.PasswordGenerator;
import com.elearning.util.PasswordStrengthChecker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField      fullNameField;
    @FXML private TextField      emailField;
    @FXML private PasswordField  passwordField;
    @FXML private PasswordField  confirmPasswordField;

    @FXML private RadioButton radioEtudiant;
    @FXML private RadioButton radioEnseignant;
    @FXML private HBox        roleHBox;

    @FXML private Label  errorLabel;
    @FXML private Label  successLabel;
    @FXML private Button btnRegister;

    // Nouveaux champs CAPTCHA
    @FXML private Canvas    captchaCanvas;
    @FXML private javafx.scene.control.Slider captchaSlider;
    @FXML private Label     errCaptcha;
    @FXML private Label     captchaInstruction;

    // Nouveaux champs force MDP
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label       passwordStrengthLabel;

    @FXML private Label       labelEmailBadge;

    private final UserService      userService;
    private final CaptchaGenerator captchaGen;
    private final EmailValidationService emailValService;
    private ToggleGroup roleGroup;

    public RegisterController() {
        this.userService = new UserService();
        this.captchaGen  = new CaptchaGenerator();
        this.emailValService = new EmailValidationService();
    }

    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        roleGroup = new ToggleGroup();
        radioEtudiant.setToggleGroup(roleGroup);
        radioEnseignant.setToggleGroup(roleGroup);
        radioEtudiant.setSelected(true);

        errorLabel.setVisible(false);   errorLabel.setManaged(false);
        successLabel.setVisible(false); successLabel.setManaged(false);
        if (errCaptcha != null) { errCaptcha.setVisible(false); errCaptcha.setManaged(false); }

        // Générer le CAPTCHA
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

        // Validation email en temps réel
        if (emailField != null) {
            emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && !emailField.getText().isBlank()) {
                    lancerValidationEmailAPI(emailField.getText().trim());
                }
            });
        }

        // Indicateur de force du mot de passe
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, old, nouveau) -> {
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
                // Bloquer soumission si mot de passe trop faible (< 3)
                if (btnRegister != null) btnRegister.setDisable(result.getScore() < 3);
            });
        }
    }


    private void lancerValidationEmailAPI(String email) {
        if (labelEmailBadge == null) return;
        labelEmailBadge.setText("🌐 Vérification...");
        labelEmailBadge.setStyle("-fx-text-fill: #95a5a6; -fx-background-color: #ecf0f1; -fx-padding: 2 6; -fx-background-radius: 4;");
        labelEmailBadge.setVisible(true);
        labelEmailBadge.setManaged(true);

        Task<EmailValidationService.ResultatValidation> task = new Task<>() {
            @Override
            protected EmailValidationService.ResultatValidation call() throws Exception {
                return emailValService.validerEmail(email);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            switch (task.getValue()) {
                case VALIDE    -> { labelEmailBadge.setText("✅ Email valide");     labelEmailBadge.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #ecf0f1; -fx-padding: 2 6; -fx-background-radius: 4;"); }
                case RISQUE    -> { labelEmailBadge.setText("⚠️ Email risqué");     labelEmailBadge.setStyle("-fx-text-fill: #e67e22; -fx-background-color: #ecf0f1; -fx-padding: 2 6; -fx-background-radius: 4;"); }
                case INVALIDE  -> { labelEmailBadge.setText("❌ Email invalide");   labelEmailBadge.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #ecf0f1; -fx-padding: 2 6; -fx-background-radius: 4;"); }
                case ERREUR_API-> { labelEmailBadge.setText("🌐 Hors-ligne");       labelEmailBadge.setStyle("-fx-text-fill: #95a5a6; -fx-background-color: #ecf0f1; -fx-padding: 2 6; -fx-background-radius: 4;"); }
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            labelEmailBadge.setText("🌐 Hors-ligne");
            labelEmailBadge.setStyle("-fx-text-fill: #95a5a6; -fx-background-color: #ecf0f1; -fx-padding: 2 6; -fx-background-radius: 4;");
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleRefreshCaptcha(ActionEvent event) {
        if (captchaCanvas != null) captchaGen.genererEtDessiner(captchaCanvas);
        if (captchaSlider != null) captchaSlider.setValue(0);
        if (captchaInstruction != null) captchaInstruction.setText(captchaGen.getReponseAttendueTexte());
        if (errCaptcha    != null) { errCaptcha.setVisible(false); errCaptcha.setManaged(false); }
    }

    @FXML
    private void handleGenererMotDePasse(ActionEvent event) {
        String motDePasseGenere = PasswordGenerator.genererMotDePasseFort();
        passwordField.setText(motDePasseGenere);
        confirmPasswordField.setText(motDePasseGenere);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe généré");
        alert.setHeaderText(null);
        alert.setContentText("Voici votre nouveau mot de passe (copiez-le) : \n\n" + motDePasseGenere);
        alert.showAndWait();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        errorLabel.setVisible(false);   errorLabel.setManaged(false);
        successLabel.setVisible(false); successLabel.setManaged(false);

        fullNameField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");

        // 1. Vérifier le CAPTCHA
        if (captchaCanvas != null && captchaSlider != null) {
            if (!captchaGen.verifier(String.valueOf(captchaSlider.getValue()))) {
                if (errCaptcha != null) {
                    errCaptcha.setText("❌ Veuillez redresser l'image correctement.");
                    errCaptcha.setVisible(true); errCaptcha.setManaged(true);
                }
                captchaGen.genererEtDessiner(captchaCanvas);
                captchaSlider.setValue(0);
                if (captchaInstruction != null) captchaInstruction.setText(captchaGen.getReponseAttendueTexte());
                return;
            }
            if (errCaptcha != null) { errCaptcha.setVisible(false); errCaptcha.setManaged(false); }
        }

        String fullName       = fullNameField.getText();
        String email          = emailField.getText();
        String password       = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        RadioButton selectedRadio = (RadioButton) roleGroup.getSelectedToggle();
        String role = (selectedRadio != null) ? selectedRadio.getUserData().toString() : User.ROLE_ETUDIANT;

        if (!password.equals(confirmPassword)) {
            afficherErreur("Les mots de passe ne correspondent pas.");
            passwordField.setStyle("-fx-border-color: red;");
            confirmPasswordField.setStyle("-fx-border-color: red;");
            return;
        }

        try {
            // Inscription via le service (vérification BDD et règles de validation)
            userService.inscrireUser(fullName, email, password, role);

            // Succès !
            successLabel.setVisible(true);
            successLabel.setManaged(true);
            
            // Vider le formulaire
            fullNameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            
        } catch (UserService.ValidationException e) {
            afficherErreur(e.getMessageFormate());
        } catch (Exception e) {
            afficherErreur("Erreur système : " + e.getMessage());
        }
    }

    @FXML
    private void allerVersLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            stage.setTitle("Eduverse — Connexion");
        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de la page de connexion.");
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
