package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.UserService;
import com.elearning.util.SessionManager;
import Utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.elearning.entity.AIRecommendation;
import com.elearning.service.AIRecommendationService;
import com.elearning.dao.AIRecommendationDAO;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * AdminDashboardController — Contrôleur principal du tableau de bord Admin.
 *
 *
 * Implements Initializable → la méthode initialize() est appelée
 * automatiquement par JavaFX une fois que le FXML est chargé.
 */
public class AdminDashboardController implements Initializable {

    // -------------------------------------------------------
    // TableView et colonnes (déclarés dans AdminDashboardView.fxml)
    // -------------------------------------------------------
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, String>  colStatut;
    @FXML private TableColumn<User, String>  colCreatedAt;
    @FXML private TableColumn<User, Void>    colActions;   // boutons Modifier/Supprimer/Bloquer

    // -------------------------------------------------------
    // Champs de recherche et filtres
    // -------------------------------------------------------
    @FXML private TextField     searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> sortColumn;
    @FXML private ComboBox<String> sortDirection;

    // -------------------------------------------------------
    // Labels statistiques (dashboard)
    // -------------------------------------------------------
    @FXML private Label totalUsersLabel;
    @FXML private Label studentsLabel;
    @FXML private Label teachersLabel;
    @FXML private Label pendingLabel;
    @FXML private Label blockedLabel;
    @FXML private Label labelNomAdmin;

    // -------------------------------------------------------
    // Recommandations IA
    // -------------------------------------------------------
    @FXML private VBox panneauIA;
    @FXML private ProgressIndicator iaLoadingIndicator;
    @FXML private Label iaStatusLabel;
    @FXML private VBox recommandationsContainer;

    // Services injectés manuellement (pas d'IoC container en Java pur)
    private final UserService userService = new UserService();
    private final com.elearning.service.PdfService pdfService = new com.elearning.service.PdfService();

    // Liste observable : quand elle change, la TableView se met à jour automatiquement
    private final ObservableList<User> listeUsers = FXCollections.observableArrayList();

    // -------------------------------------------------------
    // initialize() — appelé automatiquement après le chargement FXML
    // -------------------------------------------------------
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Configurer les colonnes de la TableView
        configurerColonnes();

        // 2. Préparer les ComboBox de filtre/tri
        configurerFiltres();

        // 3. Charger les données depuis la BDD
        chargerUtilisateurs();

        // 4. Afficher les statistiques
        updateStats();

        // 5. Afficher le nom de l'admin connecté
        User admin = SessionManager.getInstance().getUtilisateurConnecte();
        if (admin != null && labelNomAdmin != null) {
            labelNomAdmin.setText("Bonjour, " + admin.getFullName());
        }

        // 6. Recherche dynamique : recharger à chaque frappe
        searchField.textProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());
        roleFilter.valueProperty().addListener((obs, oldVal, newVal)  -> chargerUtilisateurs());
        sortColumn.valueProperty().addListener((obs, oldVal, newVal)  -> chargerUtilisateurs());
        sortDirection.valueProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());

        // 7. Charger les recommandations IA
        chargerRecommandationsIA();
    }

    // -------------------------------------------------------
    // Recommandations IA
    // -------------------------------------------------------
    private void chargerRecommandationsIA() {
        Task<List<AIRecommendation>> task = new Task<>() {
            @Override
            protected List<AIRecommendation> call() throws Exception {
                return new AIRecommendationService().analyserEtRecommander();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<AIRecommendation> recs = task.getValue();
            afficherRecommandations(recs);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            if (iaLoadingIndicator != null) iaLoadingIndicator.setVisible(false);
            if (iaStatusLabel != null) iaStatusLabel.setText("IA indisponible (vérifiez la clé API)");
        }));

        new Thread(task).start();
    }

    private void afficherRecommandations(List<AIRecommendation> recs) {
        if (iaLoadingIndicator != null) iaLoadingIndicator.setVisible(false);
        if (iaStatusLabel != null) iaStatusLabel.setText(recs.isEmpty() ? "Aucune recommandation." : recs.size() + " recommandations générées");
        if (recommandationsContainer == null) return;
        
        recommandationsContainer.getChildren().clear();
        AIRecommendationDAO dao = new AIRecommendationDAO();

        for (AIRecommendation rec : recs) {
            VBox card = new VBox(5);
            String styleClass = "rec-card-" + (rec.getPriorite() != null ? rec.getPriorite().toLowerCase() : "faible");
            card.getStyleClass().add(styleClass);

            Label lblTitre = new Label(rec.getTitre());
            lblTitre.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            Label lblDesc = new Label(rec.getDescription());
            lblDesc.setWrapText(true);
            lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #34495e;");

            Label lblAction = new Label("💡 " + rec.getActionSuggeree());
            lblAction.setWrapText(true);
            lblAction.setStyle("-fx-font-size: 11px; -fx-text-fill: #2c3e50; -fx-font-style: italic;");

            Button btnLu = new Button("✓ Marquer lue");
            btnLu.setStyle("-fx-font-size: 10px; -fx-cursor: hand;");
            btnLu.setOnAction(e -> {
                dao.marquerCommeLue(rec.getId());
                recommandationsContainer.getChildren().remove(card);
            });

            card.getChildren().addAll(lblTitre, lblDesc, lblAction, btnLu);
            recommandationsContainer.getChildren().add(card);
        }
    }



    // -------------------------------------------------------
    // Configuration des colonnes
    // -------------------------------------------------------
    private void configurerColonnes() {
        // PropertyValueFactory mappe le nom de la propriété Java au getter
        // "id" → appelle user.getId()  |  "fullName" → appelle user.getFullName()
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Pour la date, on formate en String
        colCreatedAt.setCellValueFactory(cellData -> {
            User u = cellData.getValue();
            String date = u.getCreatedAt() != null
                    ? u.getCreatedAt().toLocalDate().toString()
                    : "—";
            return new SimpleStringProperty(date);
        });

        // Colorer le statut selon sa valeur (UX améliorée)
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(switch (item) {
                        case "ACTIF"      -> "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
                        case "BLOQUE"     -> "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
                        case "EN_ATTENTE" -> "-fx-text-fill: #e67e22; -fx-font-weight: bold;";
                        default           -> "";
                    });
                }
            }
        });

        // Colonne Actions avec 3 boutons : Modifier | Supprimer | Bloquer/Débloquer
        ajouterColonneActions();

        // Lier la liste observable à la TableView
        tableUsers.setItems(listeUsers);
    }

    /**
     * Ajoute une colonne avec des boutons d'action pour chaque ligne.
     * {% for user in users %} <a href="...edit/{{ user.id }}">Modifier</a> {% endfor %}
     */
    private void ajouterColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {

            // --- Boutons icônes avec taille fixe ---
            private final Button btnModifier  = creerBouton("✏️", "btn-action-edit",    "Modifier l'utilisateur");
            private final Button btnBloquer   = creerBouton("🔒", "btn-action-block",   "Bloquer / Débloquer");
            private final Button btnFaceId    = creerBouton("📸", "btn-action-faceid",  "Enregistrer le Face ID");
            private final Button btnSupprimer = creerBouton("🗑️", "btn-action-delete",  "Supprimer l'utilisateur");
            private final HBox   boite        = new HBox(6, btnModifier, btnBloquer, btnFaceId, btnSupprimer);

            {
                boite.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // === Bouton MODIFIER ===
                btnModifier.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification(user);
                });

                // === Bouton BLOQUER/DÉBLOQUER ===
                btnBloquer.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    userService.toggleBloquer(user.getId());
                    chargerUtilisateurs();
                    updateStats();
                });

                // === Bouton FACE ID ===
                btnFaceId.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    ouvrirEnregistrementFaceId(user);
                });

                // === Bouton SUPPRIMER ===
                btnSupprimer.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Supprimer " + user.getFullName() + " ?");
                    confirm.setContentText("Cette action est irréversible.");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            User admin = SessionManager.getInstance().getUtilisateurConnecte();
                            userService.supprimerUser(user.getId(), admin.getId());
                            chargerUtilisateurs();
                            updateStats();
                            afficherSucces("Utilisateur supprimé avec succès.");
                        } catch (UserService.ValidationException ex) {
                            afficherErreur(ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    boolean bruteForceLocked = user.getLockedUntil() != null
                        && user.getLockedUntil().isAfter(java.time.LocalDateTime.now());

                    // Icône dynamique selon l'état du compte
                    if (bruteForceLocked) {
                        btnBloquer.setText("🔓");
                        javafx.scene.control.Tooltip.install(btnBloquer,
                            new javafx.scene.control.Tooltip("Débloquer (Brute Force)"));
                        btnBloquer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else if (user.isBlocked()) {
                        btnBloquer.setText("🔓");
                        javafx.scene.control.Tooltip.install(btnBloquer,
                            new javafx.scene.control.Tooltip("Débloquer l'utilisateur"));
                        btnBloquer.setStyle("");
                    } else {
                        btnBloquer.setText("🔒");
                        javafx.scene.control.Tooltip.install(btnBloquer,
                            new javafx.scene.control.Tooltip("Bloquer l'utilisateur"));
                        btnBloquer.setStyle("");
                    }
                    setGraphic(boite);
                }
            }
        });
    }

    /** Crée un bouton icône avec taille fixe et tooltip */
    private Button creerBouton(String icone, String styleClass, String tooltipText) {
        Button btn = new Button(icone);
        btn.getStyleClass().add(styleClass);
        btn.setPrefWidth(36);
        btn.setPrefHeight(36);
        btn.setStyle("-fx-font-size: 14px;");
        javafx.scene.control.Tooltip tip = new javafx.scene.control.Tooltip(tooltipText);
        javafx.scene.control.Tooltip.install(btn, tip);
        return btn;
    }

    private void ouvrirEnregistrementFaceId(User user) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/elearning/gui/FaceIdSimulatorView.fxml"));
            Stage modal = new Stage();
            modal.setTitle("📸 Enregistrement Face ID — " + user.getFullName());
            modal.setScene(new Scene(loader.load(), 460, 400));
            modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modal.getScene().getStylesheets().add(
                getClass().getResource("/com/elearning/css/style.css").toExternalForm());

            FaceIdSimulatorController ctrl = loader.getController();
            ctrl.demarrerScan(user.getEmail(), null);
            modal.show();
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir Face ID : " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Chargement des données (READ)
    // -------------------------------------------------------

    /**
     * Recharge la liste depuis la BDD en appliquant les filtres actifs.
     */
    private void chargerUtilisateurs() {
        String search    = searchField != null ? searchField.getText() : "";
        String role      = roleFilter  != null ? roleFilter.getValue()  : "";
        String col       = sortColumn  != null ? sortColumn.getValue()  : "id";
        String dir       = sortDirection != null ? sortDirection.getValue() : "ASC";

        // Mapper le libellé affiché vers le nom de colonne SQL
        String colSQL = switch (col != null ? col : "ID") {
            case "Nom"    -> "full_name";
            case "Email"  -> "email";
            case "Rôle"   -> "role";
            case "Statut" -> "statut";
            case "Date"   -> "created_at";
            default       -> "id";
        };

        // Mapper le filtre de rôle vers la valeur BDD
        String roleFiltre = (role == null || role.equals("Tous")) ? "" : role;

        List<User> users = userService.rechercherUsers(search, roleFiltre, colSQL, dir);
        listeUsers.setAll(users);
    }

    /**
     * Met à jour les compteurs statistiques du dashboard.
     */
    private void updateStats() {
        if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(userService.compterTotal()));
        if (studentsLabel != null)   studentsLabel.setText(String.valueOf(userService.compterEtudiants()));
        if (teachersLabel != null)   teachersLabel.setText(String.valueOf(userService.compterEnseignants()));
        if (pendingLabel != null)    pendingLabel.setText(String.valueOf(userService.compterEnAttente()));
        if (blockedLabel != null)    blockedLabel.setText(String.valueOf(userService.compterBloques()));
    }

    // -------------------------------------------------------
    // Formulaire Ajout/Modification (popup Modal)
    // -------------------------------------------------------

    /**
     * Ouvre un formulaire modal pour AJOUTER un nouvel utilisateur.
     */
    @FXML
    private void handleAjouter(ActionEvent event) {
        ouvrirFormulaireModification(null); // null = mode création
    }

    /**
     * Ouvre la fenêtre modale de formulaire.
     * @param user null = création, non-null = modification
     */
    private void ouvrirFormulaireModification(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/UserFormView.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au controller du formulaire
            UserFormController formController = loader.getController();
            formController.setUser(user);
            formController.setOnSaveCallback(() -> {
                chargerUtilisateurs();
                updateStats();
            });

            // Ouvrir en fenêtre modale (bloque l'accès à la fenêtre principale)
            Stage modal = new Stage();
            modal.setTitle(user == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur");
            modal.setScene(new Scene(root, 500, 550));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            modal.showAndWait();

        } catch (IOException e) {
            afficherErreur("Impossible d'ouvrir le formulaire.");
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------
    // Approbation des comptes en attente
    // -------------------------------------------------------

    @FXML
    private void handleApprouver() {
        User selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Sélectionnez un utilisateur à approuver.");
            return;
        }
        if (userService.approuverUser(selected.getId())) {
            chargerUtilisateurs();
            updateStats();
            afficherSucces(selected.getFullName() + " a été approuvé(e).");
        }
    }

    // -------------------------------------------------------
    // Export PDF
    // -------------------------------------------------------

    @FXML
    private void handleExportPdf(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter la liste des utilisateurs en PDF");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        chooser.setInitialFileName("utilisateurs_"
            + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");

        File fichier = chooser.showSaveDialog(
            ((Node) event.getSource()).getScene().getWindow());

        if (fichier != null) {
            try {
                // Fix 2 : Respecter le filtre de rôle actif au moment de l'export
                String roleActif = roleFilter.getValue() != null
                        && !roleFilter.getValue().equals("Tous")
                        ? roleFilter.getValue() : "";
                List<User> users = userService.rechercherUsers(
                    searchField.getText(), roleActif, "id", "ASC");
                pdfService.exporterListeUtilisateurs(users, fichier.getAbsolutePath());
                afficherSucces("✅ PDF exporté avec succès : " + fichier.getName());
            } catch (Exception e) {
                afficherErreur("❌ Erreur lors de l'export PDF : " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------
    // Déconnexion
    // -------------------------------------------------------

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        SessionManager.getInstance().deconnecter();
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
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------
    // Bouton Assistant IA
    // -------------------------------------------------------

    @FXML
    private void handleOuvrirIA(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/AIAssistantView.fxml"));
            Parent root = loader.load();

            javafx.stage.Stage modal = new javafx.stage.Stage();
            modal.setTitle("🤖 Assistant IA Eduverse");
            modal.setScene(new Scene(root, 520, 650));
            modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            modal.show();
        } catch (IOException e) {
            afficherErreur("❌ Impossible d'ouvrir l'assistant IA : " + e.getMessage());
        }
    }

    /**
     * Ouvre le tableau de bord des formations
     */
    @FXML
    private void handleFormations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/elearning/gui/FormationDashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Formations");
        } catch (IOException e) {
            afficherErreur(" Impossible d'ouvrir le tableau de bord des formations : " + e.getMessage());
            e.printStackTrace();
        }
    }


    // -------------------------------------------------------
    // Configuration des ComboBox
    // -------------------------------------------------------
    private void configurerFiltres() {
        if (roleFilter != null) {
            roleFilter.setItems(FXCollections.observableArrayList(
                    "Tous", User.ROLE_ADMIN, User.ROLE_ENSEIGNANT, User.ROLE_ETUDIANT));
            roleFilter.setValue("Tous");
        }
        if (sortColumn != null) {
            sortColumn.setItems(FXCollections.observableArrayList(
                    "ID", "Nom", "Email", "Rôle", "Statut", "Date"));
            sortColumn.setValue("ID");
        }
        if (sortDirection != null) {
            sortDirection.setItems(FXCollections.observableArrayList("ASC", "DESC"));
            sortDirection.setValue("ASC");
        }
    }

    // -------------------------------------------------------
    // Utilitaires d'affichage
    // -------------------------------------------------------
    private void afficherErreur(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void afficherSucces(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

