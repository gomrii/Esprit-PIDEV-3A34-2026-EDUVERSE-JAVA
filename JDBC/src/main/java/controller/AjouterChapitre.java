package controller;

import Entities.Chapitre;
import Entities.Cours;
import Services.GeminiService;
import Services.ServiceChapitre;
import Services.ServiceCours;
import javafx.application.Platform;
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
    private final GeminiService geminiService = new GeminiService();

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

    @FXML
    void handleGenerateAIContent(ActionEvent event) {
        if (titleField.getText().isEmpty()) {
            showFeedback("Erreur", "Saisissez un titre d'abord", Alert.AlertType.ERROR);
            return;
        }

        contentArea.setText("Appel à Gemini en cours...");

        new Thread(() -> {
            try {
                String result = geminiService.genererContenuChapitre(titleField.getText(),
                        coursComboBox.getValue() != null ? coursComboBox.getValue().getTitle() : "");

                javafx.application.Platform.runLater(() -> contentArea.setText(result));
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> contentArea.setText("Erreur fatale : " + ex.getMessage()));
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
                navigateToTable(event);
            } catch (SQLException e) {
                showFeedback("Erreur", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // --- AUTRES MÉTHODES (Validate, ChooseFile, Navigation) ---
    private boolean validate() {
        if (coursComboBox.getValue() == null || titleField.getText().isEmpty() || contentArea.getText().isEmpty()) {
            showFeedback("Champs vides", "Remplissez tous les champs obligatoires (*).", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML void handleChooseVideo(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) { selectedVideoPath = file.getAbsolutePath(); videoPathLabel.setText(file.getName()); }
    }

    @FXML void handleChoosePdf(ActionEvent event) {
        File file = new FileChooser().showOpenDialog(null);
        if (file != null) { selectedPdfPath = file.getAbsolutePath(); pdfPathLabel.setText(file.getName()); }
    }

    private void navigateToTable(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherChapitre.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void gotocours(ActionEvent event) { navigateToTable(event); }

    private void showFeedback(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}