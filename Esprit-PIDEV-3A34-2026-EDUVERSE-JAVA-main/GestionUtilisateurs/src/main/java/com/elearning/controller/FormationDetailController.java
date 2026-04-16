package com.elearning.controller;

import com.elearning.entity.Formation;
import com.elearning.entity.Ressource;
import com.elearning.entity.User;
import com.elearning.service.FormationService;
import com.elearning.service.RessourceService;
import com.elearning.util.SessionManager;
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
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class FormationDetailController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label levelLabel;
    @FXML private Label priceLabel;
    @FXML private Label durationLabel;
    @FXML private Label creatorLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea contentArea;
    @FXML private TableView<Ressource> ressourcesTable;
    @FXML private TableColumn<Ressource, String> colRessTitle;
    @FXML private TableColumn<Ressource, String> colRessType;
    @FXML private TableColumn<Ressource, String> colRessUrl;
    @FXML private VBox ressourcesSection;
    @FXML private TableView<Map<String, String>> enrolledStudentsTable;
    @FXML private TableColumn<Map<String, String>, String> colStudentName;
    @FXML private TableColumn<Map<String, String>, String> colStudentEmail;
    @FXML private VBox enrolledStudentsSection;
    @FXML private Button btnEnroll;
    @FXML private Button btnBack;
    @FXML private Label enrollStatusLabel;

    private final FormationService formationService = new FormationService();
    private final RessourceService ressourceService = new RessourceService();
    private Formation currentFormation;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.currentUser = SessionManager.getInstance().getUtilisateurConnecte();
        
        // Setup table columns
        colRessTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colRessType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRessUrl.setCellValueFactory(new PropertyValueFactory<>("url"));

        // Setup enrolled students table columns
        colStudentName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("studentName")));
        colStudentEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("studentEmail")));

        btnEnroll.setOnAction(event -> handleEnroll());
        btnBack.setOnAction(event -> goBack());
    }

    public void setFormation(Formation formation) {
        this.currentFormation = formation;
        displayFormationDetails();
    }

    private void displayFormationDetails() {
        titleLabel.setText(currentFormation.getTitle());
        levelLabel.setText("Niveau: " + currentFormation.getLevel());
        priceLabel.setText("Prix: " + currentFormation.getPrice() + " TND");
        durationLabel.setText("Durée: " + currentFormation.getDuration() + " heures");
        creatorLabel.setText("Créé par: " + currentFormation.getCreatorName());
        descriptionArea.setText(currentFormation.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        contentArea.setText(currentFormation.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);

        // Check if user is enrolled or is the creator/admin
        boolean isEnrolled = false;
        boolean isCreator = currentUser.getId() == currentFormation.getCreatorId();
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if ("ETUDIANT".equals(currentUser.getRole())) {
            isEnrolled = formationService.isStudentEnrolled(currentFormation.getId(), currentUser.getId());
        }

        // Show resources only if: creator/admin, or enrolled student
        if (isCreator || isAdmin || isEnrolled) {
            ressourcesSection.setVisible(true);
            loadRessources();
            btnEnroll.setVisible(false);
            enrollStatusLabel.setText("Vous avez accès à cette formation");
        } else {
            ressourcesSection.setVisible(false);
            btnEnroll.setVisible(true);
            enrollStatusLabel.setText("Les ressources seront visibles après inscription");
        }

        // Show enrolled students section only to creator/admin
        if (isCreator || isAdmin) {
            enrolledStudentsSection.setVisible(true);
            loadEnrolledStudents();
        } else {
            enrolledStudentsSection.setVisible(false);
        }
    }

    private void loadRessources() {
        ObservableList<Ressource> ressources = FXCollections.observableArrayList(
                formationService.getRessourcesForFormation(currentFormation.getId())
        );
        ressourcesTable.setItems(ressources);
    }

    private void loadEnrolledStudents() {
        ObservableList<Map<String, String>> students = FXCollections.observableArrayList(
                formationService.getEnrolledStudentsForFormation(currentFormation.getId())
        );
        enrolledStudentsTable.setItems(students);
    }

    @FXML
    private void handleEnroll() {
        try {
            boolean success = formationService.enrollStudent(currentFormation.getId(), currentUser.getId());
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Inscription réussie");
                alert.setContentText("Vous êtes maintenant inscrit à cette formation!");
                alert.showAndWait();
                displayFormationDetails(); // Refresh to show resources
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Attention");
                alert.setHeaderText("Inscription échouée");
                alert.setContentText("Vous êtes déjà inscrit à cette formation ou une erreur s'est produite.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'inscription");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}
