package controller;

import Entities.Cours;
import Services.ServiceCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ModifierCoursAdmin {

    @FXML
    private ComboBox<String> category;

    @FXML
    private TextArea descrption;

    private String pathImage;

    @FXML
    private ComboBox<String> level;

    @FXML
    private ImageView view; // Assure-toi que l'fx:id dans Scene Builder est bien "view"

    @FXML
    private TextField title;

    private Cours cours;

    @FXML
    public void initialize() {
        // ✅ Niveau
        level.getItems().addAll("Débutant", "Intermédiaire", "Avancé");

        // ✅ Category
        category.getItems().addAll(
                "Programmation", "Mathématiques", "Physique", "Design",
                "Business", "Marketing", "Reseaux", "Sécurité informatique",
                "Intelligence Artificielle", "Base de données"
        );
    }

    public void setCours(Cours cours) {
         this.cours = cours;

         title.setText(cours.getTitle());
         category.setValue(cours.getCategory());
         descrption.setText(cours.getDescrption());
         level.setValue(cours.getLevel());

         // On récupère le chemin de l'image actuelle
         this.pathImage = cours.getImage();

         // Affichage de l'image actuelle dans l'ImageView au chargement
         if (pathImage != null && !pathImage.isEmpty() && !pathImage.equals("hello")) {
             try {
                 File file = new File(pathImage.replace("\\", "/"));
                 if (file.exists()) {
                     Image img = new Image(file.toURI().toString());
                     if (!img.isError()) {
                         view.setImage(img);
                     }
                 }
             } catch (Exception e) {
                 System.err.println("Erreur chargement image : " + e.getMessage());
             }
         }
     }


    @FXML
    void choisirImage(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir l'image du cours");

        // Filtres pour restreindre aux formats d'image
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        // Récupération du stage à partir de l'événement
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fc.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // ✅ Utilisation de l'URI pour la compatibilité JavaFX (file:/C:/...)
                String imageUri = selectedFile.toURI().toString();
                Image img = new Image(imageUri, true); // true = chargement en arrière-plan

                if (!img.isError()) {
                    // On stocke l'URI dans la variable de classe pour la BDD
                    this.pathImage = imageUri;

                    // Mise à jour de l'aperçu (ImageView fx:id="view")
                    view.setImage(img);
                    view.setFitWidth(280); // Ajusté pour correspondre au design moderne
                    view.setFitHeight(180);
                    view.setPreserveRatio(true);

                    System.out.println("Image mise à jour : " + imageUri);
                } else {
                    showError("Le fichier sélectionné n'est pas une image valide.");
                }
            } catch (Exception e) {
                showError("Erreur lors du chargement de l'image : " + e.getMessage());
            }
        } else {
            System.out.println("Aucune nouvelle image sélectionnée.");
        }
    }
    @FXML
    void edit(ActionEvent event) {
        ServiceCours service = new ServiceCours();

        try {
            // --- Ton contrôle de saisie original ---
            if (title.getText() == null || title.getText().trim().isEmpty()) {
                showError("Le titre du cours est obligatoire.");
                return;
            }
            if (category.getValue() == null) {
                showError("La catégorie est obligatoire.");
                return;
            }
            if (descrption.getText() == null || descrption.getText().trim().isEmpty()) {
                showError("La description est obligatoire.");
                return;
            }
            if (level.getValue() == null) {
                showError("Le niveau est obligatoire.");
                return;
            }

            // --- Mise à jour de l'objet ---
            cours.setTitle(title.getText());
            cours.setCategory(category.getValue());
            cours.setDescrption(descrption.getText());
            cours.setLevel(level.getValue());
            cours.setImage(this.pathImage); // Enregistre le chemin (nouveau ou ancien)
            cours.setUpdated_at(new java.util.Date());

            service.update(cours);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Cours modifié avec succès !");
            alert.showAndWait();

            // --- Navigation retour ---
            navigateToTable(event);

        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToTable(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierCoursAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation : " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}