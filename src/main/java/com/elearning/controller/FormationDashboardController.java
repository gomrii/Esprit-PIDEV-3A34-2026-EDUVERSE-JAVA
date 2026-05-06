package com.elearning.controller;

import com.elearning.entity.Formation;
import com.elearning.service.FormationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FormationDashboardController implements Initializable {

    @FXML private TableView<Formation> tableFormations;
    @FXML private TableColumn<Formation, Integer> colId;
    @FXML private TableColumn<Formation, String> colTitle;
    @FXML private TableColumn<Formation, String> colPrice;
    @FXML private TableColumn<Formation, String> colLevel;
    @FXML private TableColumn<Formation, Integer> colDuration;
    @FXML private TableColumn<Formation, String> colApproved;
    @FXML private TableColumn<Formation, String> colActions;

    @FXML private TableView<Formation> tableMyFormations;
    @FXML private TableColumn<Formation, Integer> myColId;
    @FXML private TableColumn<Formation, String> myColTitle;
    @FXML private TableColumn<Formation, String> myColLevel;
    @FXML private TableColumn<Formation, Integer> myColDuration;
    @FXML private TableColumn<Formation, String> myColPrice;
    @FXML private TableColumn<Formation, String> myColApproved;
    @FXML private TableColumn<Formation, String> myColActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TabPane tabPane;

    private final FormationService formationService = new FormationService();
    private final ObservableList<Formation> formations = FXCollections.observableArrayList();
    private final ObservableList<Formation> myFormations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        tableFormations.setItems(formations);
        tableMyFormations.setItems(myFormations);
        
        initializeFilters();
        setupSearchListener();
        reload();
    }

    private void setupColumns() {
        // All formations table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colPrice.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f€", data.getValue().getPrice())));
        colApproved.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isApproved() ? "✓ Oui" : "✗ Non"));
        
        colActions.setCellValueFactory(param -> new SimpleStringProperty("..."));
        colActions.setCellFactory(col -> new TableCell<Formation, String>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            
            {
                editBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                deleteBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new javafx.scene.layout.HBox(5, editBtn, deleteBtn));
            }
        });

        // My formations table
        myColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        myColTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        myColLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        myColDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        myColPrice.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f€", data.getValue().getPrice())));
        myColApproved.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isApproved() ? "✓ Oui" : "✗ Non"));
        
        myColActions.setCellValueFactory(param -> new SimpleStringProperty("..."));
        myColActions.setCellFactory(col -> new TableCell<Formation, String>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            
            {
                editBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                deleteBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11px;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new javafx.scene.layout.HBox(5, editBtn, deleteBtn));
            }
        });
    }

    private void initializeFilters() {
        if (levelFilter != null) {
            levelFilter.setItems(FXCollections.observableArrayList(
                "Tous", "Débutant", "Intermédiaire", "Avancé", "Expert"
            ));
            levelFilter.setOnAction(e -> applyFilters());
        }
        
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList(
                "Tous", "Approuvée", "En attente", "Archivée"
            ));
            statusFilter.setOnAction(e -> applyFilters());
        }
    }

    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void applyFilters() {
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        String level = levelFilter != null ? levelFilter.getValue() : "Tous";
        String status = statusFilter != null ? statusFilter.getValue() : "Tous";

        var filtered = formations.stream()
                .filter(f -> f.getTitle().toLowerCase().contains(searchText))
                .filter(f -> "Tous".equals(level) || f.getLevel().equals(level))
                .filter(f -> applyStatusFilter(f, status))
                .collect(Collectors.toList());

        tableFormations.setItems(FXCollections.observableArrayList(filtered));
    }

    private boolean applyStatusFilter(Formation f, String status) {
        return switch(status) {
            case "Approuvée" -> f.isApproved();
            case "En attente" -> !f.isApproved() && !f.isArchived();
            case "Archivée" -> f.isArchived();
            default -> true;
        };
    }

    @FXML
    private void handleAdd() {
        openForm(null);
    }

    @FXML
    private void handleEdit(Formation formation) {
        openForm(formation);
    }

    @FXML
    private void handleDelete(Formation formation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la formation ?");
        confirm.setContentText("\"" + formation.getTitle() + "\" sera définitivement supprimée.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (formationService.delete(formation.getId())) {
                reload();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Formation supprimée avec succès.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la formation.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        reload();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Données actualisées.");
    }

    @FXML
    private void handleExportPdf() {
        showAlert(Alert.AlertType.INFORMATION, "Bientôt disponible", 
            "L'export PDF sera disponible dans une prochaine version.");
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) tableFormations.getScene().getWindow();
        stage.close();
    }

    private void openForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/FormationFormView.fxml"));
            Parent root = loader.load();

            FormationFormController controller = loader.getController();
            controller.setFormation(formation);
            controller.setOnSave(this::reload);

            Stage stage = new Stage();
            stage.setTitle(formation == null ? "Ajouter une formation" : "Modifier une formation");
            stage.setScene(new Scene(root, 900, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire.");
            e.printStackTrace();
        }
    }

    private void reload() {
        formations.setAll(formationService.findAll());
        myFormations.setAll(formations);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
