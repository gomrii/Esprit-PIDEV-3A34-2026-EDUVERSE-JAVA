package com.elearning.controller;

import com.elearning.entity.User;
import com.elearning.service.UserService;
import com.elearning.util.SessionManager;
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

/**
 * AdminDashboardController — Contrôleur principal du tableau de bord Admin.
 *
 * Équivalent Symfony : AdminController (src/Controller/Admin/AdminController.php).
 *
 * Implements Initializable → la méthode initialize() est appelée
 * automatiquement par JavaFX une fois que le FXML est chargé.
 * Équivalent Symfony : __construct() + injections de dépendances.
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
    @FXML private Label labelTotal;
    @FXML private Label labelEtudiants;
    @FXML private Label labelEnseignants;
    @FXML private Label labelActifs;
    @FXML private Label labelBloques;
    @FXML private Label labelEnAttente;
    @FXML private Label labelNomAdmin;

    // Services injectés manuellement (pas d'IoC container en Java pur)
    private final UserService userService = new UserService();
    private final com.elearning.service.PdfService pdfService = new com.elearning.service.PdfService();

    // Liste observable : quand elle change, la TableView se met à jour automatiquement
    // Équivalent Twig : la variable passée à render() qui alimente le tableau HTML
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
        actualiserStatistiques();

        // 5. Afficher le nom de l'admin connecté
        User admin = SessionManager.getInstance().getUtilisateurConnecte();
        if (admin != null && labelNomAdmin != null) {
            labelNomAdmin.setText("Bonjour, " + admin.getFullName());
        }

        // 6. Recherche dynamique : recharger à chaque frappe
        // Équivalent Symfony : la recherche se fait via ?q= en GET
        searchField.textProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());
        roleFilter.valueProperty().addListener((obs, oldVal, newVal)  -> chargerUtilisateurs());
        sortColumn.valueProperty().addListener((obs, oldVal, newVal)  -> chargerUtilisateurs());
        sortDirection.valueProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());
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
     * Équivalent Symfony/Twig : les boutons dans le tableau HTML
     * {% for user in users %} <a href="...edit/{{ user.id }}">Modifier</a> {% endfor %}
     */
    private void ajouterColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier  = new Button("✏ Modifier");
            private final Button btnSupprimer = new Button("🗑 Supprimer");
            private final Button btnBloquer   = new Button("🔒");
            private final HBox   boite        = new HBox(5, btnModifier, btnBloquer, btnSupprimer);

            {
                // Styles
                btnModifier.getStyleClass().add("btn-modifier");
                btnSupprimer.getStyleClass().add("btn-supprimer");
                btnBloquer.getStyleClass().add("btn-bloquer");

                // === Bouton MODIFIER ===
                btnModifier.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification(user);
                });

                // === Bouton SUPPRIMER ===
                btnSupprimer.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());

                    // Confirmation (équivalent JS confirm() dans Twig)
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
                            actualiserStatistiques();
                            afficherSucces("Utilisateur supprimé avec succès.");
                        } catch (UserService.ValidationException ex) {
                            afficherErreur(ex.getMessage());
                        }
                    }
                });

                // === Bouton BLOQUER/DÉBLOQUER ===
                btnBloquer.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    userService.toggleBloquer(user.getId());
                    chargerUtilisateurs();
                    actualiserStatistiques();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    btnBloquer.setText(user.isBlocked() ? "🔓 Débloquer" : "🔒 Bloquer");
                    setGraphic(boite);
                }
            }
        });
    }

    // -------------------------------------------------------
    // Chargement des données (READ)
    // -------------------------------------------------------

    /**
     * Recharge la liste depuis la BDD en appliquant les filtres actifs.
     * Équivalent Symfony : AdminController::renderUserList()
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
    private void actualiserStatistiques() {
        if (labelTotal != null)       labelTotal.setText(String.valueOf(userService.compterTotal()));
        if (labelEtudiants != null)   labelEtudiants.setText(String.valueOf(userService.compterEtudiants()));
        if (labelEnseignants != null) labelEnseignants.setText(String.valueOf(userService.compterEnseignants()));
        if (labelActifs != null)      labelActifs.setText(String.valueOf(userService.compterActifs()));
        if (labelBloques != null)     labelBloques.setText(String.valueOf(userService.compterBloques()));
        if (labelEnAttente != null)   labelEnAttente.setText(String.valueOf(userService.compterEnAttente()));
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
                actualiserStatistiques();
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
            actualiserStatistiques();
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
                List<User> users = userService.rechercherUsers(
                    searchField.getText(), "", "id", "ASC");
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
            Scene scene = new Scene(loader.load(), 500, 400);
            scene.getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(500);
            stage.setHeight(400);
            stage.centerOnScreen();
        } catch (IOException e) {
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
