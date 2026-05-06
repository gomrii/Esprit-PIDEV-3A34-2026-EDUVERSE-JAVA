package com.elearning.controller;

import com.elearning.entity.Formation;
import com.elearning.entity.User;
import com.elearning.service.FormationService;
import com.elearning.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class EnseignantDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Button btnToutesFormations;
    @FXML private Button btnMesFormations;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> sortColumn;
    @FXML private ComboBox<String> sortDirection;
    @FXML private TableView<Formation> tableFormations;
    @FXML private TableColumn<Formation, Integer> colId;
    @FXML private TableColumn<Formation, String> colTitle;
    @FXML private TableColumn<Formation, String> colCreator;
    @FXML private TableColumn<Formation, String> colLevel;
    @FXML private TableColumn<Formation, Integer> colDuration;
    @FXML private TableColumn<Formation, String> colApproval;

    @FXML private TableView<Map<String, String>> tableEnrolled;
    @FXML private TableColumn<Map<String, String>, String> colEnrFormation;
    @FXML private TableColumn<Map<String, String>, String> colEnrStudent;
    @FXML private TableColumn<Map<String, String>, String> colEnrEmail;
    @FXML private TableColumn<Map<String, String>, String> colEnrDate;

    private final FormationService formationService = new FormationService();
    private final ObservableList<Formation> formations = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> enrolledRows = FXCollections.observableArrayList();
    private boolean showOnlyMine = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getFullName());
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCreator.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCreatorName() != null ? data.getValue().getCreatorName() : "-"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colApproval.setCellValueFactory(data -> {
            Formation f = data.getValue();
            String status = f.isApproved() ? "APPROVED" : (f.isArchived() ? "DISAPPROVED" : "PENDING");
            return new SimpleStringProperty(status);
        });
        tableFormations.setItems(formations);

        // Add double-click handler to view formation details
        tableFormations.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Formation selected = tableFormations.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openFormationDetail(selected);
                }
            }
        });

        colEnrFormation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("formationTitle")));
        colEnrStudent.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("studentName")));
        colEnrEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("studentEmail")));
        colEnrDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("enrolledAt")));
        tableEnrolled.setItems(enrolledRows);

        if (levelFilter != null) {
            levelFilter.setItems(FXCollections.observableArrayList("Tous", "Beginner", "Intermediate", "Advanced"));
            levelFilter.setValue("Tous");
        }
        if (sortColumn != null) {
            sortColumn.setItems(FXCollections.observableArrayList("ID", "Title", "Level", "Duration", "Status", "Created"));
            sortColumn.setValue("ID");
        }
        if (sortDirection != null) {
            sortDirection.setItems(FXCollections.observableArrayList("DESC", "ASC"));
            sortDirection.setValue("DESC");
        }
        updateSidebarSelection();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, ov, nv) -> reloadData());
        }
        if (levelFilter != null) {
            levelFilter.valueProperty().addListener((obs, ov, nv) -> reloadData());
        }
        if (sortColumn != null) {
            sortColumn.valueProperty().addListener((obs, ov, nv) -> reloadData());
        }
        if (sortDirection != null) {
            sortDirection.valueProperty().addListener((obs, ov, nv) -> reloadData());
        }

        reloadData();
    }

    @FXML
    private void handleCreateFormation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/FormationFormView.fxml"));
            Parent root = loader.load();
            FormationFormController controller = loader.getController();
            controller.setFormation(null);
            controller.setOnSave(this::reloadData);

            Stage modal = new Stage();
            modal.setTitle("Nouvelle formation (en attente d'approbation)");
            modal.setScene(new Scene(root, 700, 560));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            modal.showAndWait();
        } catch (IOException e) {
            showError("Impossible d'ouvrir le formulaire formation.");
        }
    }

    @FXML
    private void handleRefresh() {
        reloadData();
    }

    @FXML
    private void handleShowToutesFormations() {
        showOnlyMine = false;
        updateSidebarSelection();
        reloadData();
    }

    @FXML
    private void handleShowMesFormations() {
        showOnlyMine = true;
        updateSidebarSelection();
        reloadData();
    }

    @FXML
    private void handleDeconnexion(javafx.event.ActionEvent event) {
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

    private void reloadData() {
        User current = SessionManager.getInstance().getUtilisateurConnecte();
        String search = searchField != null ? searchField.getText() : "";
        String levelUi = levelFilter != null ? levelFilter.getValue() : "Tous";
        String sortUi = sortColumn != null ? sortColumn.getValue() : "ID";
        String dir = sortDirection != null ? sortDirection.getValue() : "DESC";

        String level = (levelUi == null || "Tous".equals(levelUi)) ? "" : levelUi;
        String sortCol = switch (sortUi != null ? sortUi : "ID") {
            case "Title" -> "title";
            case "Level" -> "level";
            case "Duration" -> "duration";
            case "Status" -> "is_approved";
            case "Created" -> "created_at";
            default -> "id";
        };

        var result = formationService.searchForTeacherView(search, level, sortCol, dir);
        if (current != null && showOnlyMine) {
            result = result.stream().filter(f -> f.getCreatorId() == current.getId()).toList();
        }
        formations.setAll(result);
        if (current != null) {
            enrolledRows.setAll(formationService.findEnrolledStudentsForTeacher(current.getId()));
        }
    }

    private void updateSidebarSelection() {
        if (btnToutesFormations != null) {
            btnToutesFormations.getStyleClass().remove("sidebar-item-active");
        }
        if (btnMesFormations != null) {
            btnMesFormations.getStyleClass().remove("sidebar-item-active");
        }

        if (showOnlyMine) {
            if (btnMesFormations != null) btnMesFormations.getStyleClass().add("sidebar-item-active");
        } else {
            if (btnToutesFormations != null) btnToutesFormations.getStyleClass().add("sidebar-item-active");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
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
            showError("Impossible d'ouvrir les détails de la formation: " + e.getMessage());
        }
    }
}
