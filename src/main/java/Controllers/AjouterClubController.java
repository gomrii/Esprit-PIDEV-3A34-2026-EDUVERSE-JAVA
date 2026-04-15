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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterClubController {

    @FXML private TextField tfName;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbStatus;

    private final ServiceClub serviceClub = new ServiceClub();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "ACTIVE", "INACTIVE", "PENDING"
        ));
        cbStatus.setValue("ACTIVE");
    }

    @FXML
    private void addClub(ActionEvent ev) {
        if (!validateForm()) return;

        try {
            String name = tfName.getText().trim();
            String description = taDescription.getText().trim();
            String status = cbStatus.getValue();

            // creatorId = 1 par défaut pour le test
            Club club = new Club(name, description, status, 1);
            serviceClub.add(club);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Club ajouté avec succès !");
            goBack(ev);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Impossible d'ajouter le club.\n" + e.getMessage());
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ClubMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        if (tfName.getText().trim().isEmpty()) errors.append("- Le nom est requis\n");
        if (taDescription.getText().trim().isEmpty()) errors.append("- La description est requise\n");
        if (cbStatus.getValue() == null) errors.append("- Le statut est requis\n");

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Erreurs de validation", errors.toString());
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
