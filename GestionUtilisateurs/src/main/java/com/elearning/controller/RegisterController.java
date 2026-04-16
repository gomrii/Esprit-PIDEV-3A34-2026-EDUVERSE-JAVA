package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    
    @FXML private RadioButton radioEtudiant;
    @FXML private RadioButton radioEnseignant;
    @FXML private HBox roleHBox;

    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button btnRegister;

    private final UserService userService;
    private ToggleGroup roleGroup;  // Créé programmatiquement

    public RegisterController() {
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        // Créer le ToggleGroup programmatiquement
        roleGroup = new ToggleGroup();
        radioEtudiant.setToggleGroup(roleGroup);
        radioEnseignant.setToggleGroup(roleGroup);
        radioEtudiant.setSelected(true);  // Sélectionner l'étudiant par défaut
        
        // Cache les messages au démarrage
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // Nettoyer les messages précédents
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
        
        // Retirer les bordures rouges
        fullNameField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");

        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Récupération du rôle
        RadioButton selectedRadio = (RadioButton) roleGroup.getSelectedToggle();
        String role = (selectedRadio != null) ? selectedRadio.getUserData().toString() : User.ROLE_ETUDIANT;

        // Validation locale simple (confirmation mot de passe)
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
            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Eduverse — Connexion");
            stage.setWidth(500);
            stage.setHeight(400);
            stage.centerOnScreen();
            stage.show();
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
