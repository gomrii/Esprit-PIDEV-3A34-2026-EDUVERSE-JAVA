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



    // Assure-toi d'avoir cette variable au niveau de ta classe
    private String pathImage = "default.png";


    @FXML
    void choisier(ActionEvent event) {
        System.out.println("Clic détecté sur le bouton choisir !");

        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir l'image du cours");

        // Essaye de passer le stage actuel au lieu de 'null'
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fc.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Fichier sélectionné : " + selectedFile.getAbsolutePath());
            viewimage.setImage(new Image(selectedFile.toURI().toString()));
            viewimage.setFitWidth(200);
            viewimage.setFitHeight(200);
            viewimage.setPreserveRatio(true);
            this.pathImage = selectedFile.getAbsolutePath();
        } else {
            System.out.println("Aucun fichier n'a été sélectionné.");
        }
    }
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
    void add(ActionEvent event) {



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
            /*if (viewimage.getImage() == null) {
                showError("L'image du cours est obligatoire.");
                return;
            }*/
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
