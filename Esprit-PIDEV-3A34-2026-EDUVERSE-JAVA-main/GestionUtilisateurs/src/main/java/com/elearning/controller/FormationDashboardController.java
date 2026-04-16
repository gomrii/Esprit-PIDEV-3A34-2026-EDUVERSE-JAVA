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

public class FormationDashboardController implements Initializable {

    @FXML private TableView<Formation> tableFormations;
    @FXML private TableColumn<Formation, Integer> colId;
    @FXML private TableColumn<Formation, String> colTitle;
    @FXML private TableColumn<Formation, String> colPrice;
    @FXML private TableColumn<Formation, String> colLevel;
    @FXML private TableColumn<Formation, Integer> colDuration;
    @FXML private TableColumn<Formation, String> colApproved;

    private final FormationService formationService = new FormationService();
    private final ObservableList<Formation> formations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colPrice.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));
        colApproved.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isApproved() ? "YES" : "NO"));

        tableFormations.setItems(formations);
        reload();
    }

    @FXML
    private void handleAdd() {
        openForm(null);
    }

    @FXML
    private void handleEdit() {
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection requise", "Selectionnez une formation a modifier.");
            return;
        }
        openForm(selected);
    }

    @FXML
    private void handleDelete() {
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection requise", "Selectionnez une formation a supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la formation ?");
        confirm.setContentText(selected.getTitle());
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (formationService.delete(selected.getId())) {
                reload();
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Formation supprimee.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression echouee.");
            }
        }
    }

    private void openForm(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/elearning/gui/FormationFormView.fxml"));
            Parent root = loader.load();

            FormationFormController controller = loader.getController();
            controller.setFormation(formation);
            controller.setOnSave(this::reload);

            Stage stage = new Stage();
            stage.setTitle(formation == null ? "Ajouter formation" : "Modifier formation");
            stage.setScene(new Scene(root, 700, 560));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/com/elearning/css/style.css").toExternalForm());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire.");
        }
    }

    private void reload() {
        formations.setAll(formationService.findAll());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
