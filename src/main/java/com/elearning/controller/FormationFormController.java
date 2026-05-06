package com.elearning.controller;

import com.elearning.entity.Formation;
import com.elearning.entity.Ressource;
import com.elearning.entity.User;
import com.elearning.service.FormationService;
import com.elearning.service.GroqAIService;
import com.elearning.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FormationFormController implements Initializable {
    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextArea contentField;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private TextField supportFileField;
    @FXML private CheckBox approvedCheck;
    @FXML private CheckBox archivedCheck;
    @FXML private Button saveButton;
    @FXML private Button generateDescriptionButton;

    // Ressources fields
    @FXML private TextField ressourceTitleField;
    @FXML private TextArea ressourceDescriptionField;
    @FXML private TextField ressourceUrlField;
    @FXML private TextField ressourceCostField;
    @FXML private TextField ressourceFilePathField;
    @FXML private ComboBox<String> ressourceTypeCombo;
    @FXML private TableView<Ressource> ressourcesTable;
    @FXML private TableColumn<Ressource, String> ressourceTitleColumn;
    @FXML private TableColumn<Ressource, String> ressourceTypeColumn;
    @FXML private Button addRessourceButton;
    @FXML private Button removeRessourceButton;

    private final FormationService formationService = new FormationService();
    private final GroqAIService groqAIService = new GroqAIService();
    private Formation formationToEdit;
    private Runnable onSave;
    private List<Ressource> ressourcesToSave = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Formation level combo
        levelCombo.setItems(FXCollections.observableArrayList(
                "Beginner", "Intermediate", "Advanced"
        ));
        levelCombo.setValue("Beginner");

        // Ressource type combo
        ressourceTypeCombo.setItems(FXCollections.observableArrayList(
                "documentation", "video", "course", "hardware", "software", "book", "link", "other"
        ));
        ressourceTypeCombo.setValue("documentation");

        // Setup ressources table
        ressourceTitleColumn.setCellValueFactory(param -> 
            new javafx.beans.property.SimpleStringProperty(param.getValue().getTitle())
        );
        ressourceTypeColumn.setCellValueFactory(param -> 
            new javafx.beans.property.SimpleStringProperty(param.getValue().getType())
        );
        ressourcesTable.setItems(FXCollections.observableArrayList());

        User current = SessionManager.getInstance().getUtilisateurConnecte();
        if (current != null && User.ROLE_ENSEIGNANT.equals(current.getRole())) {
            approvedCheck.setSelected(false);
            archivedCheck.setSelected(false);
            approvedCheck.setDisable(true);
            archivedCheck.setDisable(true);
            approvedCheck.setText("Pending admin approval");
            archivedCheck.setVisible(false);
            archivedCheck.setManaged(false);
        }
    }

    public void setFormation(Formation formation) {
        this.formationToEdit = formation;

        if (formation == null) {
            titleLabel.setText("Add Formation");
            ressourcesTable.setItems(FXCollections.observableArrayList());
            ressourcesToSave.clear();
            return;
        }

        titleLabel.setText("Edit Formation");
        titleField.setText(formation.getTitle());
        descriptionField.setText(formation.getDescription());
        contentField.setText(formation.getContent());
        priceField.setText(String.valueOf(formation.getPrice()));
        durationField.setText(String.valueOf(formation.getDuration()));
        levelCombo.setValue(formation.getLevel());
        supportFileField.setText(formation.getSupportFile());
        approvedCheck.setSelected(formation.isApproved());
        archivedCheck.setSelected(formation.isArchived());

        // Load existing ressources
        List<Ressource> ressources = new ArrayList<>(formation.getRessources());
        ressourcesTable.setItems(FXCollections.observableArrayList(ressources));
        ressourcesToSave.clear();
        ressourcesToSave.addAll(ressources);
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @FXML
    private void handleSave() {
        try {
            Formation savedFormation;
            if (formationToEdit == null) {
                savedFormation = formationService.create(
                        titleField.getText(),
                        descriptionField.getText(),
                        contentField.getText(),
                        priceField.getText(),
                        durationField.getText(),
                        levelCombo.getValue(),
                        supportFileField.getText(),
                        approvedCheck.isSelected(),
                        archivedCheck.isSelected()
                );
            } else {
                formationService.update(
                        formationToEdit,
                        titleField.getText(),
                        descriptionField.getText(),
                        contentField.getText(),
                        priceField.getText(),
                        durationField.getText(),
                        levelCombo.getValue(),
                        supportFileField.getText(),
                        approvedCheck.isSelected(),
                        archivedCheck.isSelected()
                );
                savedFormation = formationToEdit;
            }

            // Save ressources
            if (savedFormation != null && savedFormation.getId() > 0) {
                // Delete existing ressources if editing
                if (formationToEdit != null) {
                    formationService.deleteRessourcesForFormation(savedFormation.getId());
                }
                // Add new ressources
                for (Ressource ressource : ressourcesToSave) {
                    formationService.createRessource(
                            ressource.getTitle(),
                            ressource.getDescription(),
                            ressource.getUrl(),
                            ressource.getType(),
                            ressource.getFilePath(),
                            ressource.getCost(),
                            savedFormation.getId()
                    );
                }
            }

            if (onSave != null) {
                onSave.run();
            }
            closeWindow();
        } catch (FormationService.ValidationException e) {
            showError(String.join("\n", e.getErrors()));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void handleBrowseSupportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Support PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File selected = chooser.showOpenDialog(saveButton.getScene().getWindow());
        if (selected != null) {
            supportFileField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleAddRessource() {
        String title = ressourceTitleField.getText().trim();
        String description = ressourceDescriptionField.getText().trim();
        String url = ressourceUrlField.getText().trim();
        String type = ressourceTypeCombo.getValue();
        String filePath = ressourceFilePathField.getText().trim();
        String costStr = ressourceCostField.getText().trim();

        if (title.isEmpty() || description.isEmpty() || url.isEmpty()) {
            showError("Please fill all required ressource fields (title, description, URL)");
            return;
        }

        Double cost = null;
        if (!costStr.isEmpty()) {
            try {
                cost = Double.parseDouble(costStr);
            } catch (NumberFormatException e) {
                showError("Cost must be a valid number");
                return;
            }
        }

        Ressource ressource = new Ressource(title, description, type, url, 
                                            filePath.isEmpty() ? null : filePath,
                                            cost, 0, 1);
        ressourcesToSave.add(ressource);
        ressourcesTable.getItems().add(ressource);

        // Clear fields
        ressourceTitleField.clear();
        ressourceDescriptionField.clear();
        ressourceUrlField.clear();
        ressourceCostField.clear();
        ressourceFilePathField.clear();
        ressourceTypeCombo.setValue("documentation");
    }

    @FXML
    private void handleRemoveRessource() {
        int selectedIndex = ressourcesTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Ressource ressource = ressourcesTable.getItems().get(selectedIndex);
            ressourcesToSave.remove(ressource);
            ressourcesTable.getItems().remove(selectedIndex);
        } else {
            showError("Please select a ressource to remove");
        }
    }

    @FXML
    private void handleGenerateDescription() {
        String title = titleField.getText().trim();
        String level = levelCombo.getValue();

        if (title.isEmpty()) {
            showError("Please enter a formation title before generating a description");
            return;
        }

        generateDescriptionButton.setDisable(true);
        generateDescriptionButton.setText("⏳ Generating...");

        // Run in a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                String generatedDescription = groqAIService.generateFormationDescription(title, level);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    descriptionField.setText(generatedDescription);
                    generateDescriptionButton.setDisable(false);
                    generateDescriptionButton.setText("🤖 Generate with AI");
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Description Generated");
                    alert.setContentText("AI-generated description has been added to the form.");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    generateDescriptionButton.setDisable(false);
                    generateDescriptionButton.setText("🤖 Generate with AI");
                    showError("Error generating description: " + e.getMessage());
                });
            }
        }).start();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
