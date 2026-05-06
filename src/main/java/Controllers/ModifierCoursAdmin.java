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

public class ModifierCoursAdmin {

    @FXML private ComboBox<String> category;
    @FXML private TextArea descrption;
    @FXML private ComboBox<String> level;
    @FXML private ImageView view;
    @FXML private TextField title;

    private String pathImage;
    private Cours cours;

    @FXML
    public void initialize() {
        // ✅ Initialisation des listes
        level.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        category.getItems().addAll(
                "Programmation", "Mathématiques", "Physique", "Design",
                "Business", "Marketing", "Reseaux", "Sécurité informatique",
                "Intelligence Artificielle", "Base de données"
        );
    }

    /**
     * Méthode appelée lors du chargement de la vue pour injecter les données du cours
     */
    public void setCours(Cours cours) {
        this.cours = cours;

        title.setText(cours.getTitle());
        category.setValue(cours.getCategory());
        descrption.setText(cours.getDescrption());
        level.setValue(cours.getLevel());
        this.pathImage = cours.getImage();

        // Chargement de l'image actuelle
        if (pathImage != null && !pathImage.isEmpty() && !pathImage.equals("hello")) {
            try {
                Image img = new Image(pathImage, true); // Utilise l'URI stockée
                view.setImage(img);
            } catch (Exception e) {
                System.err.println("Erreur chargement image : " + e.getMessage());
            }
        }
    }

    @FXML
    void choisirImage(ActionEvent event) {
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
                    this.pathImage = imageUri;
                    view.setImage(img);
                    view.setPreserveRatio(true);
                } else {
                    showError("Le fichier sélectionné n'est pas une image valide.");
                }
            } catch (Exception e) {
                showError("Erreur lors du chargement : " + e.getMessage());
            }
        }
    }

    @FXML
    void edit(ActionEvent event) {
        ServiceCours service = new ServiceCours();

        if (!validateFields()) return;

        try {
            // Mise à jour de l'objet
            cours.setTitle(title.getText().trim());
            cours.setCategory(category.getValue());
            cours.setDescrption(descrption.getText());
            cours.setLevel(level.getValue());
            cours.setImage(this.pathImage);
            cours.setUpdated_at(new java.util.Date());

            service.update(cours);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Cours modifié avec succès !");
            alert.showAndWait();

            // ✅ NAVIGATION VIA DASHBOARD (Sidebar préservée)
            MainDashboardController.getInstance().loadView("AffichierCoursAdmin.fxml", "Gestion des Cours");

        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (title.getText() == null || title.getText().trim().isEmpty()) {
            showError("Le titre est obligatoire."); return false;
        }
        if (category.getValue() == null) {
            showError("La catégorie est obligatoire."); return false;
        }
        if (descrption.getText() == null || descrption.getText().trim().isEmpty()) {
            showError("La description est obligatoire."); return false;
        }
        if (level.getValue() == null) {
            showError("Le niveau est obligatoire."); return false;
        }
        return true;
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Retour à la liste sans sauvegarder
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