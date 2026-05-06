package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.AvatarService;
import com.elearning.service.EmailValidationService;
import com.elearning.service.UserService;
import com.elearning.util.PasswordGenerator;
import com.elearning.util.PasswordStrengthChecker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
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
    @FXML private Label          passwordSection;
    @FXML private Button         btnSauvegarder;

    // Labels d'erreur inline
    @FXML private Label errNom;
    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label errRole;
    @FXML private Label errPhone;
    @FXML private Label errGlobal;

    // Nouveaux champs : Avatar
    @FXML private ImageView avatarImageView;
    @FXML private Label     labelAvatarStatus;
    @FXML private Button    btnGenererAvatar;

    // Nouveau champ : badge validation email
    @FXML private Label labelEmailBadge;

    // Nouveaux champs : force mot de passe
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label       passwordStrengthLabel;

    // -------------------------------------------------------
    // État interne
    // -------------------------------------------------------
    private User userAModifier;
    private final UserService          userService          = new UserService();
    private final AvatarService        avatarService        = new AvatarService();
    private final EmailValidationService emailValService    = new EmailValidationService();
    private Runnable onSaveCallback;
    private String   cheminAvatarLocal;   // chemin du fichier avatar local

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleCombo.setItems(FXCollections.observableArrayList(
                User.ROLE_ADMIN, User.ROLE_ENSEIGNANT, User.ROLE_ETUDIANT));
        roleCombo.setValue(User.ROLE_ETUDIANT);

        // Clip circulaire sur l'ImageView avatar
        if (avatarImageView != null) {
            Circle clip = new Circle(45, 45, 45);
            avatarImageView.setClip(clip);
            // Image par défaut
            avatarImageView.setImage(avatarService.chargerImage(null));
        }

        // Badge email masqué par défaut
        if (labelEmailBadge != null) { labelEmailBadge.setVisible(false); labelEmailBadge.setManaged(false); }

        // Validation email en temps réel (au focus-out)
        emailField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !emailField.getText().isBlank()) {
                validerEmailEnTempsReel();
                // Validation Abstract API en background
                lancerValidationEmailAPI(emailField.getText().trim());
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

        // Indicateur de force du mot de passe
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
                User cree = userService.creerUser(nom, email, password, role, phone, bio);
                // Sauvegarder le chemin de l'avatar si généré
                if (cheminAvatarLocal != null && !cheminAvatarLocal.isBlank()) {
                    cree.setPicture(cheminAvatarLocal);
                    new com.elearning.dao.UserDAO().modifierUser(cree);
                }
                afficherSuccesEtFermer("Utilisateur créé avec succès !");

            } else {
                // === MODIFICATION ===
                if (cheminAvatarLocal != null && !cheminAvatarLocal.isBlank()) {
                    userAModifier.setPicture(cheminAvatarLocal);
                }
                userService.modifierUser(userAModifier, nom, email, role, phone, bio);

                // Changer le mot de passe si un nouveau est saisi
                if (!password.isBlank()) {
                    if (!password.equals(confirm)) {
                        errPassword.setText("Les mots de passe ne correspondent pas.");
                        errPassword.setVisible(true);
                        return;
                    }
                    String hashed = userService.hasherMotDePasse(password);
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
    // Génération d'avatar DiceBear
    // -------------------------------------------------------

    @FXML
    private void handleGenererAvatar(ActionEvent event) {
        if (btnGenererAvatar != null) btnGenererAvatar.setDisable(true);
        if (labelAvatarStatus != null) labelAvatarStatus.setText("⏳ Génération en cours...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                String nom = nomField.getText().isBlank() ? "user" : nomField.getText();
                int uid = userAModifier != null ? userAModifier.getId() : 0;
                return avatarService.genererAvatar(nom, uid);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            cheminAvatarLocal = task.getValue();
            if (avatarImageView != null)
                avatarImageView.setImage(avatarService.chargerImage(cheminAvatarLocal));
            if (labelAvatarStatus != null) labelAvatarStatus.setText("✅ Avatar généré !");
            if (btnGenererAvatar != null) btnGenererAvatar.setDisable(false);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            if (labelAvatarStatus != null)
                labelAvatarStatus.setText("⚠ API indisponible — avatar par défaut utilisé");
            if (btnGenererAvatar != null) btnGenererAvatar.setDisable(false);
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleChoisirImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une photo de profil");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(nomField.getScene().getWindow());
        if (file != null) {
            cheminAvatarLocal = file.getAbsolutePath();
            if (avatarImageView != null)
                avatarImageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
            if (labelAvatarStatus != null) labelAvatarStatus.setText("✅ Image sélectionnée");
        }
    }

    @FXML
    private void handleGenererMotDePasse(ActionEvent event) {
        String motDePasseGenere = PasswordGenerator.genererMotDePasseFort();
        passwordField.setText(motDePasseGenere);
        confirmPasswordField.setText(motDePasseGenere);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe généré");
        alert.setHeaderText(null);
        alert.setContentText("Voici le nouveau mot de passe (copiez-le) : \n\n" + motDePasseGenere);
        alert.showAndWait();
    }

    // -------------------------------------------------------
    // Validation email via Abstract API (background)
    // -------------------------------------------------------

    private void lancerValidationEmailAPI(String email) {
        if (labelEmailBadge == null) return;
        labelEmailBadge.setText("🌐 Vérification...");
        labelEmailBadge.setStyle("-fx-text-fill: #95a5a6;");
        labelEmailBadge.setVisible(true);
        labelEmailBadge.setManaged(true);

        Task<EmailValidationService.ResultatValidation> task = new Task<>() {
            @Override
            protected EmailValidationService.ResultatValidation call() throws Exception {
                return emailValService.validerEmail(email);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            if (labelEmailBadge == null) return;
            switch (task.getValue()) {
                case VALIDE    -> { labelEmailBadge.setText("✅ Email valide");     labelEmailBadge.setStyle("-fx-text-fill: #27ae60;"); }
                case RISQUE    -> { labelEmailBadge.setText("⚠️ Email risqué");     labelEmailBadge.setStyle("-fx-text-fill: #e67e22;"); }
                case INVALIDE  -> { labelEmailBadge.setText("❌ Email invalide");   labelEmailBadge.setStyle("-fx-text-fill: #e74c3c;"); }
                case ERREUR_API-> { labelEmailBadge.setText("🌐 Hors-ligne");       labelEmailBadge.setStyle("-fx-text-fill: #95a5a6;"); }
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            if (labelEmailBadge != null) {
                labelEmailBadge.setText("🌐 Hors-ligne");
                labelEmailBadge.setStyle("-fx-text-fill: #95a5a6;");
            }
        }));

        new Thread(task).start();
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

