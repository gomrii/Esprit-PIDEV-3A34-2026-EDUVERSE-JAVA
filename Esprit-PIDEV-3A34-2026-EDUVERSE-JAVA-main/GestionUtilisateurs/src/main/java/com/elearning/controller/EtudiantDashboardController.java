package com.elearning.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EtudiantDashboardController implements Initializable {

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
    @FXML private TableColumn<Formation, String> colPrice;

    private final FormationService formationService = new FormationService();
    private final ObservableList<Formation> formations = FXCollections.observableArrayList();
    private boolean showOnlyEnrolled = false;

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
        colPrice.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));

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

        if (levelFilter != null) {
            levelFilter.setItems(FXCollections.observableArrayList("Tous", "Beginner", "Intermediate", "Advanced"));
            levelFilter.setValue("Tous");
        }
        if (sortColumn != null) {
            sortColumn.setItems(FXCollections.observableArrayList("ID", "Title", "Price", "Level", "Duration", "Created"));
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
    private void handleEnroll() {
        User current = SessionManager.getInstance().getUtilisateurConnecte();
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (current == null || selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection requise", "Selectionnez une formation.");
            return;
        }

        boolean enrolled = formationService.enrollStudent(selected.getId(), current.getId());
        if (enrolled) {
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Inscription effectuee.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Information", "Vous etes deja inscrit(e) a cette formation.");
        }
    }

    @FXML
    private void handleRefresh() {
        reloadData();
    }

    @FXML
    private void handleShowToutesFormations() {
        showOnlyEnrolled = false;
        updateSidebarSelection();
        reloadData();
    }

    @FXML
    private void handleShowMesFormations() {
        showOnlyEnrolled = true;
        updateSidebarSelection();
        reloadData();
    }

    @FXML
    private void handleDownloadPdf() {
        User current = SessionManager.getInstance().getUtilisateurConnecte();
        Formation selected = tableFormations.getSelectionModel().getSelectedItem();
        if (current == null || selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection requise", "Selectionnez une formation.");
            return;
        }

        if (!formationService.isStudentEnrolled(selected.getId(), current.getId())) {
            showAlert(Alert.AlertType.WARNING, "Inscription requise", "Inscrivez-vous d'abord a cette formation.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Formation PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName("formation_" + selected.getId() + ".pdf");
        File file = chooser.showSaveDialog(tableFormations.getScene().getWindow());
        if (file == null) return;

        try {
            generateFormationPdf(selected, current, file);
            showAlert(Alert.AlertType.INFORMATION, "Succes", "PDF genere: " + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Generation PDF echouee: " + e.getMessage());
        }
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
            case "Price" -> "price";
            case "Level" -> "level";
            case "Duration" -> "duration";
            case "Created" -> "created_at";
            default -> "id";
        };

        if (showOnlyEnrolled && current != null) {
            formations.setAll(formationService.searchEnrolledForStudent(current.getId(), search, level, sortCol, dir));
        } else {
            formations.setAll(formationService.searchAvailableForStudent(search, level, sortCol, dir));
        }
    }

    private void updateSidebarSelection() {
        if (btnToutesFormations != null) {
            btnToutesFormations.getStyleClass().remove("sidebar-item-active");
        }
        if (btnMesFormations != null) {
            btnMesFormations.getStyleClass().remove("sidebar-item-active");
        }

        if (showOnlyEnrolled) {
            if (btnMesFormations != null) btnMesFormations.getStyleClass().add("sidebar-item-active");
        } else {
            if (btnToutesFormations != null) btnToutesFormations.getStyleClass().add("sidebar-item-active");
        }
    }

    private void generateFormationPdf(Formation formation, User student, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        document.add(new Paragraph("Formation Enrollment"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Student: " + student.getFullName()));
        document.add(new Paragraph("Email: " + student.getEmail()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Formation ID: " + formation.getId()));
        document.add(new Paragraph("Title: " + formation.getTitle()));
        document.add(new Paragraph("Description: " + formation.getDescription()));
        document.add(new Paragraph("Level: " + formation.getLevel()));
        document.add(new Paragraph("Duration: " + formation.getDuration()));
        document.add(new Paragraph("Price: " + String.format("%.2f", formation.getPrice())));
        document.add(new Paragraph("Created by: " + (formation.getCreatorName() != null ? formation.getCreatorName() : "-")));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Content:"));
        document.add(new Paragraph(formation.getContent() != null ? formation.getContent() : ""));
        document.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails de la formation: " + e.getMessage());
        }
    }
}
