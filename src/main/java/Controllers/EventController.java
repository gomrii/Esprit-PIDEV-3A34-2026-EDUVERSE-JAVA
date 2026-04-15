package Controllers;

import Entities.Event;
import Services.ServiceEvent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class EventController implements Initializable {

    // ── Champs du formulaire ──────────────────────────────
    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpEventDate;
    @FXML private TextField tfLocation;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField tfClubId;

    // ── Boutons ───────────────────────────────────────────
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    // ── TableView et colonnes ─────────────────────────────
    @FXML private TableView<Event> tvEvents;
    @FXML private TableColumn<Event, Integer> colId;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colDescription;
    @FXML private TableColumn<Event, Date> colDate;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, String> colStatus;
    @FXML private TableColumn<Event, Integer> colClubId;

    // ── Service et données ────────────────────────────────
    private final ServiceEvent serviceEvent = new ServiceEvent();
    private ObservableList<Event> eventList;

    // ── ID de l'événement sélectionné (pour update/delete)
    private int selectedEventId = -1;

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Remplir le ComboBox des statuts
        cbStatus.setItems(FXCollections.observableArrayList(
                "upcoming", "ongoing", "completed", "cancelled"
        ));
        cbStatus.setValue("upcoming");

        // Lier les colonnes aux propriétés de Event
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colClubId.setCellValueFactory(new PropertyValueFactory<>("clubId"));

        // Charger les données
        loadEvents();

        // Quand on clique sur une ligne → remplir le formulaire
        tvEvents.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        fillForm(newVal);
                    }
                }
        );
    }

    // ══════════════════════════════════════════════════════
    //  CHARGER LES ÉVÉNEMENTS DEPUIS LA BASE
    // ══════════════════════════════════════════════════════
    private void loadEvents() {
        try {
            eventList = FXCollections.observableArrayList(serviceEvent.display());
            tvEvents.setItems(eventList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les événements:\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  AJOUTER UN ÉVÉNEMENT
    // ══════════════════════════════════════════════════════
    @FXML
    private void addEvent() {
        // Validation
        if (!validateForm()) return;

        try {
            Event event = buildEventFromForm();
            serviceEvent.add(event);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
            clearForm();
            loadEvents();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de l'ajout:\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  MODIFIER UN ÉVÉNEMENT
    // ══════════════════════════════════════════════════════
    @FXML
    private void updateEvent() {
        if (selectedEventId == -1) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un événement dans le tableau.");
            return;
        }
        if (!validateForm()) return;

        try {
            Event event = buildEventFromForm();
            event.setId(selectedEventId);
            serviceEvent.update(event);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès !");
            clearForm();
            loadEvents();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la modification:\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  SUPPRIMER UN ÉVÉNEMENT
    // ══════════════════════════════════════════════════════
    @FXML
    private void deleteEvent() {
        if (selectedEventId == -1) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un événement dans le tableau.");
            return;
        }

        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cet événement ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    Event event = new Event();
                    event.setId(selectedEventId);
                    serviceEvent.delete(event);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé !");
                    clearForm();
                    loadEvents();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la suppression:\n" + e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  EFFACER LE FORMULAIRE
    // ══════════════════════════════════════════════════════
    @FXML
    private void clearForm() {
        tfTitle.clear();
        taDescription.clear();
        dpEventDate.setValue(null);
        tfLocation.clear();
        cbStatus.setValue("upcoming");
        tfClubId.clear();
        selectedEventId = -1;
        tvEvents.getSelectionModel().clearSelection();
    }

    // ══════════════════════════════════════════════════════
    //  REMPLIR LE FORMULAIRE DEPUIS UNE LIGNE SÉLECTIONNÉE
    // ══════════════════════════════════════════════════════
    private void fillForm(Event event) {
        selectedEventId = event.getId();
        tfTitle.setText(event.getTitle());
        taDescription.setText(event.getDescription());
        tfLocation.setText(event.getLocation());
        cbStatus.setValue(event.getStatus());
        tfClubId.setText(String.valueOf(event.getClubId()));

        // Convertir java.util.Date → LocalDate pour le DatePicker
        if (event.getEventDate() != null) {
            dpEventDate.setValue(
                    event.getEventDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
            );
        }
    }

    // ══════════════════════════════════════════════════════
    //  CONSTRUIRE UN OBJET EVENT DEPUIS LE FORMULAIRE
    // ══════════════════════════════════════════════════════
    private Event buildEventFromForm() {
        String title = tfTitle.getText().trim();
        String description = taDescription.getText().trim();
        String location = tfLocation.getText().trim();
        String status = cbStatus.getValue();
        int clubId = Integer.parseInt(tfClubId.getText().trim());

        // Convertir LocalDate → java.util.Date
        Date eventDate = Date.from(
                dpEventDate.getValue()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        // creatorId = 1 par défaut (à remplacer par l'utilisateur connecté)
        return new Event(title, description, eventDate, location, status, clubId, 1);
    }

    // ══════════════════════════════════════════════════════
    //  VALIDATION DU FORMULAIRE
    // ══════════════════════════════════════════════════════
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (tfTitle.getText().trim().isEmpty())
            errors.append("• Le titre est obligatoire.\n");
        if (taDescription.getText().trim().isEmpty())
            errors.append("• La description est obligatoire.\n");
        if (dpEventDate.getValue() == null)
            errors.append("• La date est obligatoire.\n");
        if (tfLocation.getText().trim().isEmpty())
            errors.append("• Le lieu est obligatoire.\n");
        if (cbStatus.getValue() == null)
            errors.append("• Le statut est obligatoire.\n");
        if (tfClubId.getText().trim().isEmpty()) {
            errors.append("• L'ID du club est obligatoire.\n");
        } else {
            try {
                Integer.parseInt(tfClubId.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("• L'ID du club doit être un nombre.\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation", errors.toString());
            return false;
        }
        return true;
    }

    // ══════════════════════════════════════════════════════
    //  AFFICHER UNE ALERTE
    // ══════════════════════════════════════════════════════
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
