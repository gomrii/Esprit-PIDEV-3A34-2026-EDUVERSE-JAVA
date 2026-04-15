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

public class ModifierCoursEnseignant {

    @FXML
    private ComboBox<String> category;

    @FXML
    private TextArea descrption;

    @FXML
    private ComboBox<String> level;

    @FXML
    private TextField title;

    @FXML
    private ImageView view; // L'ImageView pour l'aperçu de l'image

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

    public void setCours(Cours cours) {
         this.cours = cours;

         title.setText(cours.getTitle());
         category.setValue(cours.getCategory());
         descrption.setText(cours.getDescrption());
         level.setValue(cours.getLevel());

         // Récupérer l'image actuelle
         this.pathImage = cours.getImage();

         // ✅ Afficher l'ancienne image au chargement
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
        fc.setTitle("Choisir une nouvelle image");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp")
        );

        File selectedFile = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            try {
                Image img = new Image(selectedFile.toURI().toString());
                if (!img.isError()) {
                    this.pathImage = selectedFile.getAbsolutePath();
                    view.setImage(img);
                    view.setFitWidth(200);
                    view.setFitHeight(200);
                    view.setPreserveRatio(true);
                } else {
                    showError("Erreur: Le fichier sélectionné n'est pas une image valide.");
                }
            } catch (Exception e) {
                showError("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    void edit(ActionEvent event) {
        try {
            // --- Tes contrôles de saisie originaux ---
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
            cours.setImage(this.pathImage); // Nouveau ou ancien chemin
            cours.setUpdated_at(new java.util.Date());

            service.update(cours);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Cours modifié avec succès !");
            alert.showAndWait();

            // --- Navigation retour ---
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierCoursEnseignant.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) title.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur lors de la navigation : " + e.getMessage());
            }

        } catch (SQLException e) {
            showError("Erreur SQL : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
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