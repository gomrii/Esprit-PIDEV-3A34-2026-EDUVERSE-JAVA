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

public class ModifierChapitre {

    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Label videoPathLabel, pdfPathLabel;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceCours serviceCours = new ServiceCours();

    private Chapitre chapitreToUpdate; // Garde l'ID original
    private String selectedVideoPath = "";
    private String selectedPdfPath = "";

    @FXML
    public void initialize() {
        loadCours();
    }

    private void loadCours() {
        try {
            List<Cours> list = serviceCours.display();
            coursComboBox.getItems().setAll(list);

            // Personnalisation de l'affichage dans le ComboBox
            coursComboBox.setCellFactory(lv -> new ListCell<Cours>() {
                @Override protected void updateItem(Cours item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getTitle());
                }
            });
            coursComboBox.setButtonCell(new ListCell<Cours>() {
                @Override protected void updateItem(Cours item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getTitle());
                }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // MÉTHODE CRUCIALE : Appelé par le contrôleur précédent
    public void setChapitreData(Chapitre ch) {
        this.chapitreToUpdate = ch;
        titleField.setText(ch.getTitle());
        contentArea.setText(ch.getContenu());
        selectedVideoPath = ch.getVideo();
        selectedPdfPath = ch.getPdf();
        videoPathLabel.setText(new File(ch.getVideo()).getName());
        pdfPathLabel.setText(new File(ch.getPdf()).getName());

        // Sélectionner le bon cours dans le ComboBox
        for (Cours c : coursComboBox.getItems()) {
            if (c.getId() == ch.getCours_id()) {
                coursComboBox.setValue(c);
                break;
            }
        }
    }


    @FXML
    void handleUpdate(ActionEvent event) {
        if (validate()) {
            try {
                // 1. Mise à jour de l'objet
                chapitreToUpdate.setTitle(titleField.getText());
                chapitreToUpdate.setContenu(contentArea.getText());
                chapitreToUpdate.setVideo(selectedVideoPath);
                chapitreToUpdate.setPdf(selectedPdfPath);
                chapitreToUpdate.setCours_id(coursComboBox.getValue().getId());

                // 2. Exécution SQL
                serviceChapitre.update(chapitreToUpdate);

                // 3. Message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le chapitre a été mis à jour avec succès !");
                alert.showAndWait();

                // 4. NAVIGATION INVERSE : Retour à la liste
                navigateToTable(event);

            } catch (SQLException e) {
                showAlert("Erreur", "Erreur SQL : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    private void navigateToTable(ActionEvent event) {
        try {
            // Chargement du FXML de la liste (Vérifie bien le chemin vers ton fichier)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherChapitre.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle à partir de l'événement
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // On remplace le contenu de la fenêtre par la liste
            stage.setScene(new Scene(root));

            // Optionnel : Forcer le rafraîchissement des données
            AfficherChapitre controller = loader.getController();
            controller.loadNavigationMenu(); // On appelle la méthode qui charge les données depuis la DB

            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de navigation : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void handleChooseVideo(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) {
            selectedVideoPath = file.getAbsolutePath();
            videoPathLabel.setText(file.getName());
        }
    }

    @FXML
    void handleChoosePdf(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) {
            selectedPdfPath = file.getAbsolutePath();
            pdfPathLabel.setText(file.getName());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private boolean validate() {
        if (coursComboBox.getValue() == null || titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
            showAlert("Attention", "Veuillez remplir les champs obligatoires (*).", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void showAlert(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(t); a.setContentText(m); a.showAndWait();
    }

    private AfficherChapitre parentController;

    public void setParentController(AfficherChapitre parentController) {
        this.parentController = parentController;
    }
    @FXML
    void gotochapitres(ActionEvent event) {
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