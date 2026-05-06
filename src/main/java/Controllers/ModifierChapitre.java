package Controllers;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.ServiceCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class ModifierChapitre {

    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Label videoPathLabel, pdfPathLabel;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceCours serviceCours = new ServiceCours();

    private Chapitre chapitreToUpdate;
    private String selectedVideoPath = "";
    private String selectedPdfPath = "";
    private AfficherChapitre parentController;

    public void setParentController(AfficherChapitre parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        loadCours();
    }

    private void loadCours() {
        try {
            List<Cours> list = serviceCours.display();
            coursComboBox.getItems().setAll(list);

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

    /**
     * Reçoit les données du chapitre à modifier depuis le contrôleur précédent.
     */
    public void setChapitreData(Chapitre ch) {
        this.chapitreToUpdate = ch;
        titleField.setText(ch.getTitle());
        contentArea.setText(ch.getContenu());
        selectedVideoPath = ch.getVideo();
        selectedPdfPath = ch.getPdf();

        // Gestion sécurisée de l'affichage des noms de fichiers
        if (ch.getVideo() != null && !ch.getVideo().isEmpty()) {
            videoPathLabel.setText(new File(ch.getVideo()).getName());
        }
        if (ch.getPdf() != null && !ch.getPdf().isEmpty()) {
            pdfPathLabel.setText(new File(ch.getPdf()).getName());
        }

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
                chapitreToUpdate.setTitle(titleField.getText());
                chapitreToUpdate.setContenu(contentArea.getText());
                chapitreToUpdate.setVideo(selectedVideoPath);
                chapitreToUpdate.setPdf(selectedPdfPath);
                chapitreToUpdate.setCours_id(coursComboBox.getValue().getId());

                serviceChapitre.update(chapitreToUpdate);

                showAlert("Succès", "Le chapitre a été mis à jour avec succès !", Alert.AlertType.INFORMATION);

                // ✅ Retour à la liste via le Dashboard
                MainDashboardController.getInstance().loadView("AfficherChapitre.fxml", "Gestion des Chapitres");

            } catch (SQLException e) {
                showAlert("Erreur", "Erreur SQL : " + e.getMessage(), Alert.AlertType.ERROR);
            }
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
        // ✅ Retour à la liste sans enregistrer
        MainDashboardController.getInstance().loadView("AfficherChapitre.fxml", "Gestion des Chapitres");
    }

    private boolean validate() {
        if (coursComboBox.getValue() == null || titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
            showAlert("Attention", "Veuillez remplir les champs obligatoires (*).", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void showAlert(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    @FXML
    void gotochapitres(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherChapitre.fxml", "Gestion des Chapitres");
    }


}