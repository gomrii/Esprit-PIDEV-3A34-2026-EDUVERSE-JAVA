package com.elearning.controller;

import com.elearning.entity.Formation;
import com.elearning.entity.User;
import com.elearning.service.FormationService;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // Users tab
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, String> colCreatedAt;
    @FXML private TableColumn<User, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> sortColumn;
    @FXML private ComboBox<String> sortDirection;

    // Formation tab
    @FXML private TabPane mainTabs;
    @FXML private Tab formationsTab;
    @FXML private TableView<Formation> tableFormations;
    @FXML private TableColumn<Formation, Integer> colFormationId;
    @FXML private TableColumn<Formation, String> colFormationTitle;
    @FXML private TableColumn<Formation, String> colFormationPrice;
    @FXML private TableColumn<Formation, String> colFormationLevel;
    @FXML private TableColumn<Formation, Integer> colFormationDuration;
    @FXML private TableColumn<Formation, String> colFormationApproved;
    @FXML private TextField formationSearchField;
    @FXML private ComboBox<String> formationLevelFilter;
    @FXML private ComboBox<String> formationSortColumn;
    @FXML private ComboBox<String> formationSortDirection;

    // Dashboard labels
    @FXML private Label labelTotal;
    @FXML private Label labelEtudiants;
    @FXML private Label labelEnseignants;
    @FXML private Label labelActifs;
    @FXML private Label labelBloques;
    @FXML private Label labelEnAttente;
    @FXML private Label labelNomAdmin;

    private final UserService userService = new UserService();
    private final FormationService formationService = new FormationService();
    private final com.elearning.service.PdfService pdfService = new com.elearning.service.PdfService();

    private final ObservableList<User> listeUsers = FXCollections.observableArrayList();
    private final ObservableList<Formation> listeFormations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerColonnesUsers();
        configurerFiltresUsers();
        chargerUtilisateurs();
        actualiserStatistiques();

        configurerColonnesFormations();
        configurerFiltresFormations();
        chargerFormations();

        User admin = SessionManager.getInstance().getUtilisateurConnecte();
        if (admin != null && labelNomAdmin != null) {
            labelNomAdmin.setText("Bonjour, " + admin.getFullName());
        }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());
        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());
        sortColumn.valueProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());
        sortDirection.valueProperty().addListener((obs, oldVal, newVal) -> chargerUtilisateurs());

        if (formationSearchField != null) {
            formationSearchField.textProperty().addListener((obs, oldVal, newVal) -> chargerFormations());
        }
        if (formationLevelFilter != null) {
            formationLevelFilter.valueProperty().addListener((obs, oldVal, newVal) -> chargerFormations());
        }
        if (formationSortColumn != null) {
            formationSortColumn.valueProperty().addListener((obs, oldVal, newVal) -> chargerFormations());
        }
        if (formationSortDirection != null) {
            formationSortDirection.valueProperty().addListener((obs, oldVal, newVal) -> chargerFormations());
        }
    }

    private void configurerColonnesUsers() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCreatedAt.setCellValueFactory(cellData -> {
            User u = cellData.getValue();
            String date = u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : "-";
            return new SimpleStringProperty(date);
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final Button btnBloquer = new Button("Bloquer");
            private final HBox box = new HBox(5, btnModifier, btnBloquer, btnSupprimer);

            {
                btnModifier.getStyleClass().add("btn-modifier");
                btnSupprimer.getStyleClass().add("btn-supprimer");
                btnBloquer.getStyleClass().add("btn-bloquer");

                btnModifier.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireUser(user);
                });

                btnSupprimer.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText("Supprimer " + user.getFullName() + " ?");
                    confirm.setContentText("Action irreversible.");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            User admin = SessionManager.getInstance().getUtilisateurConnecte();
                            userService.supprimerUser(user.getId(), admin.getId());
                            chargerUtilisateurs();
                            actualiserStatistiques();
                            afficherSucces("Utilisateur supprime.");
                        } catch (UserService.ValidationException ex) {
                            afficherErreur(ex.getMessage());
                        }
                    }
                });

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
                    btnBloquer.setText(user.isBlocked() ? "Debloquer" : "Bloquer");
                    setGraphic(box);
                }
            }
        });

        tableUsers.setItems(listeUsers);
    }

    private void configurerColonnesFormations() {
        colFormationId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFormationTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colFormationLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colFormationDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colFormationPrice.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));
        colFormationApproved.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isApproved() ? "YES" : "NO"));
        tableFormations.setItems(listeFormations);

        // Add double-click handler to view formation details
        tableFormations.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Formation selected = tableFormations.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openFormationDetail(selected);
                }
            }
        });
    }

    private void chargerUtilisateurs() {
        String search = searchField != null ? searchField.getText() : "";
        String role = roleFilter != null ? roleFilter.getValue() : "";
        String col = sortColumn != null ? sortColumn.getValue() : "id";
        String dir = sortDirection != null ? sortDirection.getValue() : "ASC";

        String colSQL = switch (col != null ? col : "ID") {
            case "Nom" -> "full_name";
            case "Email" -> "email";
            case "Role" -> "role";
            case "Statut" -> "statut";
            case "Date" -> "created_at";
            default -> "id";
        };

        String roleFiltre = (role == null || role.equals("Tous")) ? "" : role;
        List<User> users = userService.rechercherUsers(search, roleFiltre, colSQL, dir);
        listeUsers.setAll(users);
    }

    private void chargerFormations() {
        String search = formationSearchField != null ? formationSearchField.getText() : "";
        String levelUi = formationLevelFilter != null ? formationLevelFilter.getValue() : "Tous";
        String sortUi = formationSortColumn != null ? formationSortColumn.getValue() : "ID";
        String sortDir = formationSortDirection != null ? formationSortDirection.getValue() : "DESC";

        String levelFilter = (levelUi == null || levelUi.equals("Tous")) ? "" : levelUi;
        String sortColumn = switch (sortUi != null ? sortUi : "ID") {
            case "Title" -> "title";
            case "Price" -> "price";
            case "Level" -> "level";
            case "Duration" -> "duration";
            case "Approved" -> "is_approved";
            case "Created" -> "created_at";
            default -> "id";
        };

        listeFormations.setAll(
                formationService.rechercherFormations(search, levelFilter, sortColumn, sortDir)
        );
    }

    private void actualiserStatistiques() {
        if (labelTotal != null) labelTotal.setText(String.valueOf(userService.compterTotal()));
        if (labelEtudiants != null) labelEtudiants.setText(String.valueOf(userService.compterEtudiants()));
        if (labelEnseignants != null) labelEnseignants.setText(String.valueOf(userService.compterEnseignants()));
        if (labelActifs != null) labelActifs.setText(String.valueOf(userService.compterActifs()));
        if (labelBloques != null) labelBloques.setText(String.valueOf(userService.compterBloques()));
        if (labelEnAttente != null) labelEnAttente.setText(String.valueOf(userService.compterEnAttente()));
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        ouvrirFormulaireUser(null);
    }

    @FXML
    private void handleApprouver() {
        User selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Selectionnez un utilisateur a approuver.");
            return;
        }
        if (userService.approuverUser(selected.getId())) {
            chargerUtilisateurs();
            actualiserStatistiques();
            afficherSucces(selected.getFullName() + " approuve.");
        }
    }

    @FXML
    private void handleExporterPDF() {
        try {
            if (listeUsers.isEmpty()) {
                afficherErreur("La liste est vide.");
                return;
            }
            java.io.File destFile = new java.io.File("Utilisateurs_Export.pdf");
            pdfService.exporterListeUtilisateurs(listeUsers, destFile.getAbsolutePath());
            afficherSucces("PDF genere: " + destFile.getAbsolutePath());
        } catch (Exception e) {
            afficherErreur("Erreur export PDF: " + e.getMessage());
        }
    }

    @FXML
    private void handleFormations() {
        if (mainTabs != null && formationsTab != null) {
            mainTabs.getSelectionModel().select(formationsTab);
        }
    }

    @FXML
    private void handleFormationAjouter() {
        ouvrirFormulaireFormation(null);
    }

    @FXML
    private void handleFormationModifier() {
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Selectionnez une formation a modifier.");
            return;
        }
        ouvrirFormulaireFormation(selected);
    }

    @FXML
    private void handleFormationSupprimer() {
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Selectionnez une formation a supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la formation ?");
        confirm.setContentText(selected.getTitle());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (formationService.delete(selected.getId())) {
                chargerFormations();
                afficherSucces("Formation supprimee.");
            } else {
                afficherErreur("Suppression echouee.");
            }
        }
    }

    @FXML
    private void handleFormationApprouver() {
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Selectionnez une formation a approuver.");
            return;
        }
        if (formationService.approveFormation(selected.getId())) {
            chargerFormations();
            afficherSucces("Formation approuvee.");
        } else {
            afficherErreur("Approbation echouee.");
        }
    }

    @FXML
    private void handleFormationRefuser() {
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Selectionnez une formation a refuser.");
            return;
        }
        if (formationService.disapproveFormation(selected.getId())) {
            chargerFormations();
            afficherSucces("Formation refusee.");
        } else {
            afficherErreur("Refus echoue.");
        }
    }

    private void ouvrirFormulaireUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/UserFormView.fxml"));
            Parent root = loader.load();
            UserFormController formController = loader.getController();
            formController.setUser(user);
            formController.setOnSaveCallback(() -> {
                chargerUtilisateurs();
                actualiserStatistiques();
            });

            Stage modal = new Stage();
            modal.setTitle(user == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur");
            modal.setScene(new Scene(root, 500, 550));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            modal.showAndWait();
        } catch (IOException e) {
            afficherErreur("Impossible d'ouvrir le formulaire utilisateur.");
        }
    }

    private void ouvrirFormulaireFormation(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/FormationFormView.fxml"));
            Parent root = loader.load();
            FormationFormController controller = loader.getController();
            controller.setFormation(formation);
            controller.setOnSave(this::chargerFormations);

            Stage modal = new Stage();
            modal.setTitle(formation == null ? "Ajouter formation" : "Modifier formation");
            modal.setScene(new Scene(root, 700, 560));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            modal.showAndWait();
        } catch (IOException e) {
            afficherErreur("Impossible d'ouvrir le formulaire formation.");
        }
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        SessionManager.getInstance().deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 500, 400);
            scene.getStylesheets().add(getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(500);
            stage.setHeight(400);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configurerFiltresUsers() {
        if (roleFilter != null) {
            roleFilter.setItems(FXCollections.observableArrayList(
                    "Tous", User.ROLE_ADMIN, User.ROLE_ENSEIGNANT, User.ROLE_ETUDIANT));
            roleFilter.setValue("Tous");
        }
        if (sortColumn != null) {
            sortColumn.setItems(FXCollections.observableArrayList(
                    "ID", "Nom", "Email", "Role", "Statut", "Date"));
            sortColumn.setValue("ID");
        }
        if (sortDirection != null) {
            sortDirection.setItems(FXCollections.observableArrayList("ASC", "DESC"));
            sortDirection.setValue("ASC");
        }
    }

    private void configurerFiltresFormations() {
        if (formationLevelFilter != null) {
            formationLevelFilter.setItems(FXCollections.observableArrayList(
                    "Tous", "Beginner", "Intermediate", "Advanced"));
            formationLevelFilter.setValue("Tous");
        }
        if (formationSortColumn != null) {
            formationSortColumn.setItems(FXCollections.observableArrayList(
                    "ID", "Title", "Price", "Level", "Duration", "Approved", "Created"));
            formationSortColumn.setValue("ID");
        }
        if (formationSortDirection != null) {
            formationSortDirection.setItems(FXCollections.observableArrayList("DESC", "ASC"));
            formationSortDirection.setValue("DESC");
        }
    }

    private void afficherErreur(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void afficherSucces(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void openFormationDetail(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/FormationDetailView.fxml"));
            Parent root = loader.load();
            FormationDetailController controller = loader.getController();
            controller.setFormation(formation);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Formation");
            stage.setScene(new Scene(root));
            stage.setWidth(1000);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            afficherErreur("Impossible d'ouvrir les détails de la formation: " + e.getMessage());
        }
    }
}
