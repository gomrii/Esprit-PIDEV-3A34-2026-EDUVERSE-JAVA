package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * UserFormController — Formulaire d'ajout/modification d'utilisateur.
 *
 * dans AdminController.
 *
 * Ce controller est réutilisé pour :
 *   - La CRÉATION (user == null au démarrage)
 *   - La MODIFICATION (user != null, champs pré-remplis)
 */
public class UserFormController implements Initializable {

    // -------------------------------------------------------
    // Champs du formulaire
    // -------------------------------------------------------
    @FXML private TextField      nomField;
    @FXML private TextField      emailField;
    @FXML private PasswordField  passwordField;
    @FXML private PasswordField  confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField      phoneField;
    @FXML private TextArea       bioArea;
    @FXML private Label          titreLabel;
    @FXML private Label          passwordSection;   // cachée en mode modification
    @FXML private Button         btnSauvegarder;

    // Labels d'erreur inline (un par champ)
    @FXML private Label errNom;
    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label errRole;
    @FXML private Label errPhone;
    @FXML private Label errGlobal;

    // -------------------------------------------------------
    // État interne
    // -------------------------------------------------------
    private User userAModifier;         // null = mode création
    private final UserService userService = new UserService();
    private Runnable onSaveCallback;    // callback appelé après sauvegarde réussie

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remplir la ComboBox des rôles
        roleCombo.setItems(FXCollections.observableArrayList(
                User.ROLE_ADMIN, User.ROLE_ENSEIGNANT, User.ROLE_ETUDIANT));
        roleCombo.setValue(User.ROLE_ETUDIANT);

        // Validation en temps réel sur le champ email
        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                validerEmailEnTempsReel();
            }
        });

        // Validation mot de passe en temps réel
        confirmPasswordField.textProperty().addListener((obs, o, n) -> {
            if (!n.equals(passwordField.getText())) {
                errPassword.setText("Les mots de passe ne correspondent pas.");
                errPassword.setVisible(true);
            } else {
                errPassword.setVisible(false);
            }
        });
    }

    /**
     * Injecte l'utilisateur à modifier (appelé par AdminDashboardController).
     * Si user == null → mode création.
     * Si user != null → mode modification (pré-remplissage).
     *
     * + $form = $this->createForm(UserFormType::class, $user)
     */
    public void setUser(User user) {
        this.userAModifier = user;

        if (user == null) {
            // Mode création
            titreLabel.setText("➕ Ajouter un utilisateur");
            afficherSectionMotDePasse(true);

        } else {
            // Mode modification : pré-remplir les champs
            titreLabel.setText("✏ Modifier : " + user.getFullName());
            nomField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            roleCombo.setValue(user.getRole());
            phoneField.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            bioArea.setText(user.getBio() != null ? user.getBio() : "");

            // En modification, le mot de passe n'est pas requis
            afficherSectionMotDePasse(false);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    // -------------------------------------------------------
    // Sauvegarde (CREATE ou UPDATE)
    // -------------------------------------------------------

    @FXML
    private void handleSauvegarder(ActionEvent event) {
        // Réinitialiser tous les messages d'erreur
        cacherTousLesErreurs();

        String nom     = nomField.getText().trim();
        String email   = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        String role    = roleCombo.getValue();
        String phone   = phoneField.getText().trim();
        String bio     = bioArea.getText().trim();

        // Vérification des mots de passe identiques (côté UI uniquement)
        if (userAModifier == null && !password.equals(confirm)) {
            errPassword.setText("Les deux mots de passe doivent être identiques.");
            errPassword.setVisible(true);
            return;
        }

        try {
            if (userAModifier == null) {
                // === CRÉATION ===
                userService.creerUser(nom, email, password, role, phone, bio);
                afficherSuccesEtFermer("Utilisateur créé avec succès !");

            } else {
                // === MODIFICATION ===
                userService.modifierUser(userAModifier, nom, email, role, phone, bio);

                // Changer le mot de passe si un nouveau est saisi
                if (!password.isBlank()) {
                    if (!password.equals(confirm)) {
                        errPassword.setText("Les mots de passe ne correspondent pas.");
                        errPassword.setVisible(true);
                        return;
                    }
                    // Le Service hashe avant d'envoyer au DAO
                    // (ici on appelle directement le DAO via UserService)
                    String hashed = userService.hasherMotDePasse(password);
                    // On réutilise la connexion via UserDAO interne du service
                    // Pour simplifier, on réouvre via UserDAO directement
                    new com.elearning.dao.UserDAO().changerMotDePasse(userAModifier.getId(), hashed);
                }

                afficherSuccesEtFermer("Utilisateur modifié avec succès !");
            }

        } catch (UserService.ValidationException e) {
            // Afficher les erreurs de validation
            afficherErreursValidation(e.getErreurs());
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        fermerFenetre();
    }

    // -------------------------------------------------------
    // Validation temps réel sur l'email
    // -------------------------------------------------------
    private void validerEmailEnTempsReel() {
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errEmail.setText("Format d'e-mail invalide.");
            errEmail.setVisible(true);
        } else {
            errEmail.setVisible(false);
        }
    }

    // -------------------------------------------------------
    // Utilitaires d'affichage
    // -------------------------------------------------------

    private void afficherErreursValidation(java.util.List<String> erreurs) {
        // Afficher chaque erreur dans le bon label selon son contenu
        for (String err : erreurs) {
            String lower = err.toLowerCase();
            if (lower.contains("nom"))         { errNom.setText(err);      errNom.setVisible(true); }
            else if (lower.contains("email") || lower.contains("e-mail")) {
                errEmail.setText(err); errEmail.setVisible(true);
            }
            else if (lower.contains("mot de passe")) { errPassword.setText(err); errPassword.setVisible(true); }
            else if (lower.contains("rôle"))    { errRole.setText(err);     errRole.setVisible(true); }
            else if (lower.contains("téléphone")) { errPhone.setText(err);  errPhone.setVisible(true); }
            else { errGlobal.setText(err);      errGlobal.setVisible(true); }
        }
    }

    private void cacherTousLesErreurs() {
        for (Label l : new Label[]{errNom, errEmail, errPassword, errRole, errPhone, errGlobal}) {
            if (l != null) { l.setText(""); l.setVisible(false); }
        }
    }

    private void afficherSectionMotDePasse(boolean visible) {
        if (passwordSection  != null) passwordSection.setVisible(visible);
        if (passwordSection  != null) passwordSection.setManaged(visible);
        passwordField.setVisible(visible);
        passwordField.setManaged(visible);
        confirmPasswordField.setVisible(visible);
        confirmPasswordField.setManaged(visible);
    }

    private void afficherSuccesEtFermer(String message) {
        if (onSaveCallback != null) onSaveCallback.run();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnSauvegarder.getScene().getWindow();
        stage.close();
    }
}

