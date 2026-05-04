package Controllers;

import Utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectionController {

    @FXML
    void goAdmin(ActionEvent event) {
        Session.role = "ADMIN";
        Session.userId = 1; // ID Admin fixe
        loadPage(event, "/MainDashboard.fxml");
    }

    @FXML
    void goEtudiant(ActionEvent event) {
        Session.role = "ETUDIANT";
        Session.userId = 2; // ID Étudiant fixe
        loadPage(event, "/EtudiantDashboard.fxml");
    }

    @FXML
    void goEnseignant(ActionEvent event) {
        Session.role = "ENSEIGNANT";
        Session.userId = 3; // ID Enseignant fixe
        loadPage(event, "/EnseignantDashboard.fxml");
    }

    /**
     * Méthode générique pour charger une nouvelle page FXML.
     */
    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Récupérer le Stage actuel via l'événement
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            Scene scene = new Scene(root);
            // Optionnel : Ajouter le CSS si nécessaire
            // scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page : " + fxmlPath);
        }
    }
}
