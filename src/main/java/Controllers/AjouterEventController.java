package Controllers;

import Entities.Event;
import Services.ServiceEvent;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import Services.GeminiService;
import Utils.Session;

public class AjouterEventController {

    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpEventDate;
    @FXML private TextField tfLocation;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<Entities.Club> cbClubId;
    @FXML private Button btnGenerateAI;

    private final ServiceEvent serviceEvent = new ServiceEvent();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "APPROVED", "PENDING", "REJECTED"
        ));
        cbStatus.setValue("APPROVED");

        // Masquer le choix du statut pour les étudiants
        if (!"ADMIN".equals(Session.role)) {
            cbStatus.setVisible(false);
        }

        // Charger les clubs
        loadClubs();

        // Real-time cleanup of error styles
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        tfTitle.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(tfTitle));
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(taDescription));
        tfLocation.textProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(tfLocation));
        dpEventDate.valueProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(dpEventDate));
        cbStatus.valueProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(cbStatus));
        cbClubId.valueProperty().addListener((obs, oldVal, newVal) -> clearErrorStyle(cbClubId));
    }

    private void loadClubs() {
        try {
            Services.ServiceClub sc = new Services.ServiceClub();
            java.util.List<Entities.Club> clubs;
            if ("ADMIN".equals(Session.role)) {
                clubs = sc.display();
            } else {
                clubs = sc.getClubsByCreator(Session.userId);
            }
            cbClubId.setItems(FXCollections.observableArrayList(clubs));
            
            // Custom display for the ComboBox
            cbClubId.setConverter(new javafx.util.StringConverter<Entities.Club>() {
                @Override public String toString(Entities.Club club) {
                    return club == null ? "" : club.getName() + " (ID: " + club.getId() + ")";
                }
                @Override public Entities.Club fromString(String string) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addEvent(ActionEvent ev) {
        if (!validateEvent()) return;
        
        if (Utils.MyDb.getInstance().getConn() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "La connexion à la base de données a échoué.");
            return;
        }

        try {
            String title = tfTitle.getText().trim();
            String description = taDescription.getText().trim();
            String location = tfLocation.getText().trim();
            String status = cbStatus.getValue();
            
            Entities.Club selectedClub = cbClubId.getValue();
            if (selectedClub == null) {
                setErrorStyle(cbClubId);
                showAlert(Alert.AlertType.WARNING, "Contrôle de saisie", "Veuillez sélectionner un club.");
                return;
            }
            int clubId = selectedClub.getId();

            Date eventDate = Date.from(dpEventDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // --- WORKFLOW DE VALIDATION ---
            if (!"ADMIN".equals(Session.role)) {
                status = "PENDING";
            }

            Event event = new Event(title, description, eventDate, location, status, clubId, Session.userId);
            serviceEvent.add(event);

            if ("ETUDIANT".equals(Session.role)) {
                showAlert(Alert.AlertType.INFORMATION, "Demande Envoyée", "Votre événement est en attente de validation par l'administrateur.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
            }
            goBack(ev);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Événements");
    }

    @FXML
    private void generateAIDescription(ActionEvent event) {
        String title = tfTitle.getText().trim();
        String location = tfLocation.getText().trim();

        // 1. Validation obligatoire
        if (title.isEmpty()) {
            setErrorStyle(tfTitle);
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Titre obligatoire");
            return;
        }

        if (dpEventDate.getValue() == null) {
            setErrorStyle(dpEventDate);
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Date obligatoire");
            return;
        }
        
        if (location.isEmpty()) {
            setErrorStyle(tfLocation);
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Lieu obligatoire");
            return;
        }

        String dateStr = dpEventDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // 2. Génération de la description via IA
        btnGenerateAI.setDisable(true);
        btnGenerateAI.setText("⏳ Génération IA...");
        
        new Thread(() -> {
            try {
                String aiDescription = GeminiService.generateDescription(title, dateStr, location);
                javafx.application.Platform.runLater(() -> {
                    taDescription.setText(aiDescription);
                    clearErrorStyle(taDescription);
                    btnGenerateAI.setDisable(false);
                    btnGenerateAI.setText("✨ Générer par IA");
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur AI", "Impossible de générer la description.");
                    btnGenerateAI.setDisable(false);
                    btnGenerateAI.setText("✨ Générer par IA");
                });
            }
        }).start();
    }

    @FXML
    private void openMapPicker(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MapPicker.fxml"));
            Parent root = loader.load();
            
            MapPickerController controller = loader.getController();
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Sélectionner un lieu sur la carte");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la carte : " + e.getMessage());
        }
    }

    public void setLocationFromMap(String address) {
        tfLocation.setText(address);
        clearErrorStyle(tfLocation);
    }

    private boolean validateEvent() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (tfTitle.getText().trim().length() < 3) {
            errors.append("- Titre : minimum 3 caractères.\n");
            setErrorStyle(tfTitle);
            isValid = false;
        }

        if (taDescription.getText().trim().length() < 10) {
            errors.append("- Description : minimum 10 caractères.\n");
            setErrorStyle(taDescription);
            isValid = false;
        }

        if (tfLocation.getText().trim().isEmpty()) {
            errors.append("- Lieu : ne peut pas être vide.\n");
            setErrorStyle(tfLocation);
            isValid = false;
        }

        if (dpEventDate.getValue() == null) {
            errors.append("- Date : obligatoire.\n");
            setErrorStyle(dpEventDate);
            isValid = false;
        } else if (dpEventDate.getValue().isBefore(java.time.LocalDate.now())) {
            errors.append("- Date : doit être dans le futur.\n");
            setErrorStyle(dpEventDate);
            isValid = false;
        }

        if (cbStatus.getValue() == null || cbStatus.getValue().isEmpty()) {
            errors.append("- Statut : veuillez sélectionner un statut.\n");
            setErrorStyle(cbStatus);
            isValid = false;
        }

        if (!isValid) {
            showAlert(Alert.AlertType.WARNING, "Contrôle de saisie", errors.toString());
        }

        return isValid;
    }

    private void setErrorStyle(Control control) {
        control.getStyleClass().add("error-field");
    }

    private void clearErrorStyle(Control control) {
        control.getStyleClass().remove("error-field");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
