package Controllers;

import Entities.Cours;
import Services.ServiceCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AjouterCoursAdmin {

    @FXML private Button ajouter;
    @FXML private ComboBox<String> category;
    @FXML private TextArea descrption;
    @FXML private Button image;
    @FXML private ComboBox<String> level;
    @FXML private TextField title;
    @FXML private ImageView viewimage;

    private String pathImage = "";

    @FXML
    public void initialize() {
        // ✅ Initialisation du Niveau
        level.getItems().addAll(
                "Débutant",
                "Intermédiaire",
                "Avancé"
        );

        // ✅ Initialisation des Catégories
        category.getItems().addAll(
                "Programmation",
                "Mathématiques",
                "Physique",
                "Design",
                "Business",
                "Marketing",
                "Reseaux",
                "Sécurité informatique",
                "Intelligence Artificielle",
                "Base de données"
        );
    }

    @FXML
    void choisier(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir l'image du cours");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fc.showOpenDialog(stage);

        if (selectedFile != null) {
            this.pathImage = selectedFile.toURI().toString();
            viewimage.setImage(new Image(this.pathImage));
        }
    }

    @FXML
    void add(ActionEvent event) {
        String titreSaisi = title.getText().trim();
        ServiceCours service = new ServiceCours();

        if (!validateInput(titreSaisi, service)) {
            return;
        }

        try {
            Cours c = new Cours();
            c.setTitle(titreSaisi);
            c.setCategory(category.getValue());
            c.setDescrption(descrption.getText());
            c.setLevel(level.getValue());
            c.setImage(pathImage);
            c.setStatus("approuved");
            c.setCreated_at(new java.util.Date());
            c.setUpdated_at(new java.util.Date());

            service.add(c);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Cours ajouté avec succès !");
            alert.showAndWait();

            // ✅ NAVIGATION VIA DASHBOARD (Sidebar conservée)
            MainDashboardController.getInstance().loadView("AffichierCoursAdmin.fxml", "Gestion des Cours");

        } catch (SQLException e) {
            showError("Erreur BDD : " + e.getMessage());
        }
    }

    private boolean validateInput(String titreSaisi, ServiceCours service) {
        if (titreSaisi.isEmpty()) {
            showError("Le titre du cours est obligatoire.");
            return false;
        }
        if (category.getValue() == null) {
            showError("La catégorie est obligatoire.");
            return false;
        }
        if (descrption.getText() == null || descrption.getText().trim().isEmpty()) {
            showError("La description est obligatoire.");
            return false;
        }
        if (level.getValue() == null) {
            showError("Le niveau est obligatoire.");
            return false;
        }
        if (pathImage.isEmpty()) {
            showError("Veuillez choisir une image pour le cours.");
            return false;
        }
        try {
            if (service.exists(titreSaisi)) {
                showError("Erreur : Un cours avec le titre '" + titreSaisi + "' existe déjà !");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @FXML
    void annuler(ActionEvent event) {
        // ✅ Retour à la table au lieu de juste effacer (optionnel selon ton besoin)
        MainDashboardController.getInstance().loadView("AffichierCoursAdmin.fxml", "Gestion des Cours");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}