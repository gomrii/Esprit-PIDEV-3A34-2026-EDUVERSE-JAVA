package controller;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.ServiceCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterChapitre {

    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Label videoPathLabel;
    @FXML private Label pdfPathLabel;
    @FXML private Label chapitreLabel;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceCours serviceCours = new ServiceCours();

    private String selectedVideoPath = "";
    private String selectedPdfPath = "";

    @FXML
    public void initialize() {
        try {
            // Remplir le ComboBox avec les cours de la base de données
            List<Cours> list = serviceCours.display();
            coursComboBox.getItems().setAll(list);

            // Afficher le titre du cours dans la liste déroulante
            coursComboBox.setCellFactory(param -> new ListCell<Cours>() {
                @Override
                protected void updateItem(Cours item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getTitle());
                }
            });
            coursComboBox.setButtonCell(new ListCell<Cours>() {
                @Override
                protected void updateItem(Cours item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getTitle());
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleChooseVideo(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vidéo MP4", "*.mp4"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            selectedVideoPath = file.getAbsolutePath();
            videoPathLabel.setText(file.getName());
        }
    }

    @FXML
    void handleChoosePdf(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            selectedPdfPath = file.getAbsolutePath();
            pdfPathLabel.setText(file.getName());
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (validate()) {
            try {
                // Création de l'objet Chapitre
                Chapitre ch = new Chapitre();
                ch.setTitle(titleField.getText());
                ch.setContenu(contentArea.getText());
                ch.setVideo(selectedVideoPath);
                ch.setPdf(selectedPdfPath);
                ch.setCours_id(coursComboBox.getValue().getId());

                // Appel du service
                serviceChapitre.add(ch);

                showFeedback("Succès", "Le chapitre '" + ch.getTitle() + "' a été ajouté.", Alert.AlertType.INFORMATION);
                resetForm();
            } catch (SQLException e) {
                showFeedback("Erreur", "Problème lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validate() {
        if (coursComboBox.getValue() == null || titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
            showFeedback("Champs obligatoires", "Veuillez remplir le cours, le titre et le contenu.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void showFeedback(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void resetForm() {
        titleField.clear();
        contentArea.clear();
        videoPathLabel.setText("Aucun fichier");
        pdfPathLabel.setText("Aucun fichier");
        selectedVideoPath = "";
        selectedPdfPath = "";
    }
    @FXML
    void gotocours(ActionEvent event) {
        try {
            // Chargement du fichier FXML de l'administration des cours
            // Vérifie bien que le nom est exactement "AffichierCoursAdmin.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherChapitre.fxml"));
            Parent root = loader.load();

            // Récupération du Stage (la fenêtre) actuel
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Création et application de la nouvelle scène
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Optionnel : centrer la fenêtre si la taille change
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur de navigation vers Admin Cours : " + e.getMessage());
            e.printStackTrace();

            // Alerte visuelle en cas d'erreur (fichier manquant par exemple)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger la page AffichierCoursAdmin.");
            alert.show();
        }
    }
}