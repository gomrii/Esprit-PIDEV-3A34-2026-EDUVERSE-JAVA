package Controllers;

import Entities.Chapitre;
import Entities.Cours;
import Services.GeminService;
import Services.ServiceChapitre;
import Services.ServiceCours;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AjouterChapitre {

    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Label videoPathLabel;
    @FXML private Label pdfPathLabel;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceCours serviceCours = new ServiceCours();
    private final GeminService geminiService = new GeminService(); // Conservé

    private String selectedVideoPath = "";
    private String selectedPdfPath = "";

    @FXML
    public void initialize() {
        try {
            List<Cours> list = serviceCours.display();
            coursComboBox.getItems().setAll(list);

            coursComboBox.setCellFactory(param -> new ListCell<Cours>() {
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

    // --- LOGIQUE GEMINI CONSERVÉE ---
    @FXML
    void handleGenerateAIContent(ActionEvent event) {
        if (titleField.getText().isEmpty()) {
            showFeedback("Erreur", "Saisissez un titre d'abord pour guider l'IA.", Alert.AlertType.ERROR);
            return;
        }

        contentArea.setText("🤖 Gemini génère votre contenu... Veuillez patienter.");

        new Thread(() -> {
            try {
                String result = geminiService.genererContenuChapitre(
                        titleField.getText(),
                        coursComboBox.getValue() != null ? coursComboBox.getValue().getTitle() : ""
                );

                Platform.runLater(() -> contentArea.setText(result));
            } catch (Exception ex) {
                Platform.runLater(() -> contentArea.setText("Erreur lors de la génération : " + ex.getMessage()));
            }
        }).start();
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (validate()) {
            try {
                Chapitre ch = new Chapitre();
                ch.setTitle(titleField.getText());
                ch.setContenu(contentArea.getText());
                ch.setVideo(selectedVideoPath);
                ch.setPdf(selectedPdfPath);
                ch.setCours_id(coursComboBox.getValue().getId());

                serviceChapitre.add(ch);
                showFeedback("Succès", "Chapitre ajouté avec succès.", Alert.AlertType.INFORMATION);

                // Navigation via Dashboard pour garder la sidebar
                MainDashboardController.getInstance().loadView("AfficherChapitre.fxml", "Gestion des Chapitres");
            } catch (SQLException e) {
                showFeedback("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // --- GESTION DES FICHIERS ---
    @FXML void handleChooseVideo(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) { selectedVideoPath = file.getAbsolutePath(); videoPathLabel.setText(file.getName()); }
    }

    @FXML void handleChoosePdf(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) { selectedPdfPath = file.getAbsolutePath(); pdfPathLabel.setText(file.getName()); }
    }

    // --- NAVIGATION ---
    @FXML
    void gotocours(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherChapitre.fxml", "Gestion des Chapitres");
    }

    private boolean validate() {
        if (coursComboBox.getValue() == null || titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
            showFeedback("Champs obligatoires", "Le titre, le cours et le contenu sont nécessaires.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void showFeedback(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}