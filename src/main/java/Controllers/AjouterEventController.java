package Controllers;

import Entities.Event;
import Services.ServiceEvent;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

public class AjouterEventController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpEventDate;
    @FXML private TextField tfLocation;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField tfClubId;

    private final ServiceEvent serviceEvent = new ServiceEvent();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "upcoming", "ongoing", "completed", "cancelled"
        ));
        cbStatus.setValue("upcoming");
    }

    @FXML
    private void addEvent(ActionEvent ev) {
        if (!validateForm()) return;

        try {
            String title = tfTitle.getText().trim();
            String description = taDescription.getText().trim();
            String location = tfLocation.getText().trim();
            String status = cbStatus.getValue();
            int clubId = Integer.parseInt(tfClubId.getText().trim());

            Date eventDate = Date.from(dpEventDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // creatorId = 1 par défaut (à adapter plus tard)
            Event event = new Event(title, description, eventDate, location, status, clubId, 1);
            serviceEvent.add(event);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
            goBack(ev);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Assurez-vous que l'ID Club existe et est valide.\n" + e.getMessage());
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        navigateTo(event, "/EventMenu.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        if (tfTitle.getText().isEmpty()) errors.append("- Titre requis\n");
        if (taDescription.getText().isEmpty()) errors.append("- Description requise\n");
        if (tfLocation.getText().isEmpty()) errors.append("- Lieu requis\n");
        if (dpEventDate.getValue() == null) errors.append("- Date requise\n");
        try {
            Integer.parseInt(tfClubId.getText());
        } catch (Exception e) {
            errors.append("- Club ID doit être un nombre valide\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Erreurs", errors.toString());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
