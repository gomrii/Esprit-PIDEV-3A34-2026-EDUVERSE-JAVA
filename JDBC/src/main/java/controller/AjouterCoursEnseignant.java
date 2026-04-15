
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

public class AjouterCoursEnseignant {



    @FXML
    private ComboBox<String> category;

    @FXML
    private TextArea descrption;



    @FXML
    private ComboBox<String> level;

    @FXML
    private TextField title;

    @FXML
    private ImageView viewimage;

    private String pathImage;


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
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToTable(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierCoursEnseignant.fxml"));
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
    void add(ActionEvent event) {


        ServiceCours service = new ServiceCours();

        Cours c = new Cours();
        c.setTitle(title.getText());
        c.setCategory(category.getValue().toString());
        c.setDescrption(descrption.getText());
        c.setLevel(level.getValue().toString());
        c.setImage(pathImage);
        c.setStatus("pending");
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
                try {
                    // 3. Ajout en base de données
                    service.add(c);

                    // 4. Alerte spécifique pour informer l'enseignant
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Soumission réussie");
                    alert.setHeaderText("Cours en attente de validation");
                    alert.setContentText("Votre cours a été ajouté avec succès !\n\n" +
                            "Note : Il sera visible par les étudiants dès que l'administrateur l'aura approuvé.");

                    // On attend que l'utilisateur clique sur OK
                    alert.showAndWait();

                    // 5. Redirection automatique
                    navigateToTable(event);

                } catch (SQLException e) {
                    showError("Erreur lors de l'ajout : " + e.getMessage());
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void choisier(ActionEvent event) {
        System.out.println("Clic détecté sur le bouton choisir !"); // <--- AJOUTE ÇA

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
    void annuler(ActionEvent event)throws SQLException {

        title.clear();
        descrption.clear();
        category.setValue(null);
        level.setValue(null);
        viewimage.setImage(null);


    }
}


