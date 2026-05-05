package Controllers;

import Entities.Club;
import Services.ServiceClub;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import Utils.Session;

public class AjouterClubController {

    @FXML private TextField tfName;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbStatus;
    @FXML private VBox statusContainer;

    private final ServiceClub serviceClub = new ServiceClub();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "APPROVED", "REJECTED"
        ));
        cbStatus.setValue("APPROVED");
        
        // Seul l'admin peut choisir le statut directement
        if (!"ADMIN".equals(Session.role)) {
            statusContainer.setVisible(false);
            statusContainer.setManaged(false); 
        }
        
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        tfName.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(tfName));
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(taDescription));
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(cbStatus));
    }

    @FXML
    private void addClub(ActionEvent ev) {
        if (!validateClub()) return;
        
        if (Utils.MyDb.getInstance().getConn() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "La connexion à la base de données a échoué.");
            return;
        }

        try {
            String name = tfName.getText().trim();
            String description = taDescription.getText().trim();
            String status = cbStatus.getValue();

            // Création avec statut PENDING si non-admin
            Club club = new Club(name, description, status, Session.userId);
            if (!"ADMIN".equals(Session.role)) {
                club.setStatus("PENDING");
            }

            serviceClub.add(club);

            if (!"ADMIN".equals(Session.role)) {
                showAlert(Alert.AlertType.INFORMATION, "Demande Envoyée", "Votre demande de création de club est en attente de validation.");
                // Mise à jour du label de rôle dans le dashboard
                MainDashboardController.getInstance().updateRoleLabel();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Club créé avec succès !");
            }
            goBack(ev);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Impossible d'ajouter le club.\n" + e.getMessage());
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    private boolean validateClub() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (tfName.getText().trim().length() < 3) {
            errors.append("- Nom : minimum 3 caractères.\n");
            setErrorStyle(tfName);
            isValid = false;
        }

        if (taDescription.getText().trim().isEmpty()) {
            errors.append("- Description : ne peut pas être vide.\n");
            setErrorStyle(taDescription);
            isValid = false;
        }

        if ("ADMIN".equals(Session.role)) {
            if (cbStatus.getValue() == null || cbStatus.getValue().isEmpty()) {
                errors.append("- Statut : veuillez sélectionner un statut.\n");
                setErrorStyle(cbStatus);
                isValid = false;
            }
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Contrôle de saisie", errors.toString());
        }

        return isValid;
    }

    private void setErrorStyle(Control control) {
        control.getStyleClass().add("error-field");
    }

    private void clearErrorStyle(Control control) {
        control.getStyleClass().remove("error-field");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
