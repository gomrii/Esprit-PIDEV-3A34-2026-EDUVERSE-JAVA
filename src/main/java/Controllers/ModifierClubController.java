package Controllers;

import Entities.Club;
import Services.ServiceClub;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ModifierClubController {

    @FXML private TextField tfName;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbStatus;

    private final ServiceClub serviceClub = new ServiceClub();
    private int selectedClubId = -1;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "PENDING"));
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        tfName.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(tfName));
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(taDescription));
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(cbStatus));
    }

    /**
     * Called from AfficherClubController to pass the club to modify.
     */
    public void setClub(Club club) {
        if (club == null) return;
        
        this.selectedClubId = club.getId();
        tfName.setText(club.getName());
        taDescription.setText(club.getDescription());
        cbStatus.setValue(club.getStatus());
    }

    @FXML
    private void updateClub(ActionEvent ev) {
        if (selectedClubId == -1) return;
        if (!validateClub()) return;

        if (Utils.MyDb.getInstance().getConn() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "La connexion à la base de données a échoué.");
            return;
        }

        try {
            Club club = new Club();
            club.setId(selectedClubId);
            club.setName(tfName.getText().trim());
            club.setDescription(taDescription.getText().trim());
            club.setStatus(cbStatus.getValue());

            serviceClub.update(club);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Club mis à jour avec succès !");
            goBack(ev);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
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

        if (cbStatus.getValue() == null || cbStatus.getValue().isEmpty()) {
            errors.append("- Statut : veuillez sélectionner un statut.\n");
            setErrorStyle(cbStatus);
            isValid = false;
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

    @FXML
    private void goBack(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }
}
