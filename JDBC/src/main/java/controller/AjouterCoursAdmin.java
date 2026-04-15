package controller;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import Entities.Cours;
import Services.ServiceCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import java.io.File;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterCoursAdmin {

    @FXML
    private Button ajouter;

    @FXML
    private ComboBox<String> category;

    @FXML
    private TextArea descrption;

    @FXML
    private Button image;

    @FXML
    private ComboBox<String> level;

    @FXML
    private TextField title;

    @FXML
    private ImageView viewimage;
    @FXML
    private Button addc;
    // Au lieu de : private String pathImage = "default.png";
    private String pathImage = ""; // On initialise à vide







    @FXML
    public void initialize() {

        // ✅ Niveau
        level.getItems().addAll(
                List.of(
                        "Débutant",
                        "Intermédiaire",
                        "Avancé"
                )
        );
        // ✅ Category (full list)
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
        // On cible la colonne 'image'

    }
    @FXML
    void choisier(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir l'image du cours");

        // Filtre pour n'accepter que des images
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fc.showOpenDialog(stage);

        if (selectedFile != null) {
            // ✅ ON STOCK l'URI (file:/C:/...) au lieu du chemin brut
            this.pathImage = selectedFile.toURI().toString();

            // Affichage immédiat dans l'ImageView
            viewimage.setImage(new Image(this.pathImage));

            // Optionnel : Mettre à jour le label "Aucune image"
            // Si tu as un Label fx:id="imageLabel"
            // imageLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    void add(ActionEvent event) {


        String titreSaisi = title.getText().trim();
        ServiceCours service = new ServiceCours();

        Cours c = new Cours();
        c.setTitle(title.getText());
        c.setCategory(category.getValue().toString());
        c.setDescrption(descrption.getText());
        c.setLevel(level.getValue().toString());
        c.setImage(pathImage);
        c.setStatus("approuved");
        // Initialisation des dates pour éviter le NullPointerException
        c.setCreated_at(new java.util.Date());
        c.setUpdated_at(new java.util.Date());
        try {
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
            if (pathImage == null || pathImage.isEmpty()) {
                showError("Veuillez choisir une image pour le cours.");
                return;
            }
            // 2. TEST D'UNICITÉ
            if (service.exists(titreSaisi)) {
                showError("Erreur : Un cours avec le titre '" + titreSaisi + "' existe déjà !");
                return; // On arrête l'exécution ici
            }
            else {
                service.add(c);
                // 4. Alerte de succès (Attend le clic sur OK)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Cours ajouté avec succès !");
                alert.showAndWait();

                // 5. Navigation vers la liste
                navigateToTable(event);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.show();
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
            e.printStackTrace();
        }
    }


    @FXML
    void annuler(ActionEvent event)throws SQLException {

            title.clear();
            descrption.clear();
            category.setValue(null);
            level.setValue(null);
            viewimage.setImage(null);


        }


}
