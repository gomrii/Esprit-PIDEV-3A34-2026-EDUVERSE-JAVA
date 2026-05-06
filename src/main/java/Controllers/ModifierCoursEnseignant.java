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

public class ModifierCoursEnseignant {

    @FXML private ComboBox<String> category;
    @FXML private TextArea descrption;
    @FXML private ComboBox<String> level;
    @FXML private TextField title;
    @FXML private ImageView view;

    private String pathImage;
    private Cours cours;
    private final ServiceCours service = new ServiceCours();

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
     * Reçoit les données du cours à modifier (appelé via loadViewWithData)
     */
    public void setCours(Cours cours) {
        this.cours = cours;

        title.setText(cours.getTitle());
        category.setValue(cours.getCategory());
        descrption.setText(cours.getDescrption());
        level.setValue(cours.getLevel());
        this.pathImage = cours.getImage();

        // ✅ Affichage de l'image actuelle
        if (pathImage != null && !pathImage.isEmpty()) {
            try {
                Image img = new Image(pathImage, true);
                if (!img.isError()) {
                    view.setImage(img);
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image : " + e.getMessage());
            }
        }
    }

    @FXML
    void choisirImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une nouvelle image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        File selectedFile = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

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
            MainDashboardController.getInstance().loadView("AffichierCoursEnseignant.fxml", "Mes Cours");

        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Retour à la liste sans sauvegarder
        MainDashboardController.getInstance().loadView("AffichierCoursEnseignant.fxml", "Mes Cours");
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}