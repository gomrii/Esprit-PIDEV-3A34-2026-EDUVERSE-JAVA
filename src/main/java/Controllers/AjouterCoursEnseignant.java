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

public class AjouterCoursEnseignant {

    @FXML private ComboBox<String> category;
    @FXML private TextArea descrption;
    @FXML private ComboBox<String> level;
    @FXML private TextField title;
    @FXML private ImageView viewimage;

    private String pathImage = ""; // Initialisation à vide pour éviter le null

    @FXML
    public void initialize() {
        // ✅ Initialisation des Niveaux
        level.getItems().addAll("Débutant", "Intermédiaire", "Avancé");

        // ✅ Initialisation des Catégories
        category.getItems().addAll(
                "Programmation", "Mathématiques", "Physique", "Design", "Business",
                "Marketing", "Reseaux", "Sécurité informatique",
                "Intelligence Artificielle", "Base de données"
        );
    }

    @FXML
    void add(ActionEvent event) {
        String titreSaisi = title.getText().trim();
        ServiceCours service = new ServiceCours();

        if (!validateInput()) {
            return;
        }

        try {
            Cours c = new Cours();
            c.setTitle(titreSaisi);
            c.setCategory(category.getValue());
            c.setDescrption(descrption.getText());
            c.setLevel(level.getValue());
            c.setImage(pathImage);

            // Pour l'enseignant, le statut par défaut est toujours 'pending'
            c.setStatus("pending");

            c.setCreated_at(new java.util.Date());
            c.setUpdated_at(new java.util.Date());

            service.add(c);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Soumission réussie");
            alert.setHeaderText("Cours en attente de validation");
            alert.setContentText("Votre cours a été ajouté avec succès !\n\n" +
                    "Note : Il sera visible dès que l'administrateur l'aura approuvé.");
            alert.showAndWait();

            // ✅ NAVIGATION VIA DASHBOARD (Sidebar conservée)
            MainDashboardController.getInstance().loadView("AffichierCoursEnseignant.fxml", "Mes Cours");

        } catch (SQLException e) {
            showError("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    void choisier(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir l'image du cours");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fc.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String imageUri = selectedFile.toURI().toString();
                Image img = new Image(imageUri, true);

                if (!img.isError()) {
                    viewimage.setImage(img);
                    viewimage.setPreserveRatio(true);
                    this.pathImage = imageUri;
                } else {
                    showError("Le fichier sélectionné n'est pas une image valide.");
                }
            } catch (Exception e) {
                showError("Erreur lors du chargement : " + e.getMessage());
            }
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        // Redirection vers la liste des cours pour éviter de rester bloqué sur le formulaire
        MainDashboardController.getInstance().loadView("AffichierCoursEnseignant.fxml", "Mes Cours");
    }

    private boolean validateInput() {
        if (title.getText() == null || title.getText().trim().isEmpty()) {
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
        if (pathImage == null || pathImage.isEmpty()) {
            showError("L'image du cours est obligatoire.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}