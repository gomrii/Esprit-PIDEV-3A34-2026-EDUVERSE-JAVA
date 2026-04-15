package com.elearning.controller;

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

/**
 * LoginController — Contrôleur de la page de connexion.
 *
 * Équivalent Symfony : SecurityController + AppAuthenticator.
 *
 * @FXML : annotation qui lie les éléments de LoginView.fxml
 *         aux variables Java. JavaFX les injecte automatiquement
 *         (comme l'injection de dépendances Symfony).
 */
public class LoginController {

    // -------------------------------------------------------
    // Injection des éléments FXML (déclarés dans LoginView.fxml)
    // -------------------------------------------------------
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UserService userService = new UserService();



    /**
     * Appelé au clic sur "Se connecter".
     * Équivalent Symfony : AppAuthenticator::authenticate()
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        // Réinitialiser les erreurs
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        clearErrors();

        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            // Authentification via le Service (validation + vérification BDD)
            User user = userService.authentifier(email, password);

            // Stocker l'utilisateur en session
            SessionManager.getInstance().connecter(user);

            // Rediriger vers la bonne interface selon le rôle
            // Équivalent Symfony : onAuthenticationSuccess() → redirection selon ROLE_*
            String fxmlPath = switch (user.getRole()) {
                case User.ROLE_ADMIN      -> "/com/elearning/gui/AdminDashboardView.fxml";
                case User.ROLE_ENSEIGNANT -> "/com/elearning/gui/EnseignantDashboardView.fxml";
                default                   -> "/com/elearning/gui/EtudiantDashboardView.fxml";
            };

            naviguerVers(fxmlPath, event);

        } catch (UserService.ValidationException e) {
            // Afficher le message d'erreur dans l'interface
            afficherErreur(e.getMessage());
        }
    }

    /**
     * Navigue vers une autre vue JavaFX.
     * Équivalent Symfony : $this->redirectToRoute('admin_dashboard')
     */
    private void naviguerVers(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());

            stage.setScene(scene);
            
            // ✅ FORCER LES DIMENSIONS
            com.elearning.util.WindowHelper.fixDimensions(stage);
            
            stage.show();

        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de l'interface.");
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {
        errorLabel.setText("⚠  " + message);
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

    /**
     * Navigation vers la page d'inscription.
     */
    @FXML
    private void allerVersInscription(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/RegisterView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/com/elearning/css/style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Eduverse — Inscription");
            
            // ✅ FORCER LES DIMENSIONS
            com.elearning.util.WindowHelper.fixDimensions(stage);

        } catch (IOException e) {
            afficherErreur("Erreur lors du chargement de l'interface.");
            e.printStackTrace();
        }
    }
}
