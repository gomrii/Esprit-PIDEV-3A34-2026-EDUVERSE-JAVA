package Controllers;

import Entities.Event;
import Services.ServiceEvent;
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
import java.time.ZoneId;
import java.util.Date;

public class ModifierEventController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpEventDate;
    @FXML private TextField tfLocation;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField tfClubId;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private int selectedEventId = -1;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("upcoming", "ongoing", "completed", "cancelled"));
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        tfTitle.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(tfTitle));
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(taDescription));
        tfLocation.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(tfLocation));
        dpEventDate.valueProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(dpEventDate));
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(cbStatus));
    }

    /**
     * Called from AfficherEventController to pass the event to modify.
     */
    public void setEvent(Event event) {
        if (event == null) return;
        
        this.selectedEventId = event.getId();
        tfTitle.setText(event.getTitle());
        taDescription.setText(event.getDescription());
        tfLocation.setText(event.getLocation());
        cbStatus.setValue(event.getStatus());
        tfClubId.setText(String.valueOf(event.getClubId()));

        if (event.getEventDate() != null) {
            Date eventDate = event.getEventDate();
            if (eventDate instanceof java.sql.Date) {
                dpEventDate.setValue(((java.sql.Date) eventDate).toLocalDate());
            } else {
                dpEventDate.setValue(eventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }
    }

    @FXML
    private void updateEvent(ActionEvent ev) {
        if (selectedEventId == -1) return;
        if (!validateEvent()) return;

        if (Utils.MyDb.getInstance().getConn() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "La connexion à la base de données a échoué.");
            return;
        }

        try {
            Event event = new Event();
            event.setId(selectedEventId);
            event.setTitle(tfTitle.getText().trim());
            event.setDescription(taDescription.getText().trim());
            event.setLocation(tfLocation.getText().trim());
            event.setStatus(cbStatus.getValue());
            
            try { 
                event.setClubId(Integer.parseInt(tfClubId.getText().trim())); 
            } catch (NumberFormatException e) {
                setErrorStyle(tfClubId);
                showAlert(Alert.AlertType.WARNING, "Contrôle de saisie", "L'ID du club doit être un nombre.");
                return;
            }

            if (dpEventDate.getValue() != null) {
                event.setEventDate(Date.from(dpEventDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            serviceEvent.update(event);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement mis à jour avec succès !");
            goBack(ev);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    private boolean validateEvent() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (tfTitle.getText().trim().length() < 3) {
            errors.append("- Titre : minimum 3 caractères.\n");
            setErrorStyle(tfTitle);
            isValid = false;
        }

        if (taDescription.getText().trim().length() < 10) {
            errors.append("- Description : minimum 10 caractères.\n");
            setErrorStyle(taDescription);
            isValid = false;
        }

        if (tfLocation.getText().trim().isEmpty()) {
            errors.append("- Lieu : ne peut pas être vide.\n");
            setErrorStyle(tfLocation);
            isValid = false;
        }

        if (dpEventDate.getValue() == null) {
            errors.append("- Date : obligatoire.\n");
            setErrorStyle(dpEventDate);
            isValid = false;
        } else if (dpEventDate.getValue().isBefore(java.time.LocalDate.now())) {
            errors.append("- Date : doit être dans le futur.\n");
            setErrorStyle(dpEventDate);
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
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Événements");
    }
}
