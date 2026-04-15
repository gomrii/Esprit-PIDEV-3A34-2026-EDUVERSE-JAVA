package Controllers;

import Entities.Event;
import Services.ServiceEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

public class ModifierEventController {

    @FXML private TableView<Event> tvEvents;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, Date> colDate;
    
    @FXML private VBox formModifier;
    @FXML private TextField tfTitle;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpEventDate;
    @FXML private TextField tfLocation;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField tfClubId;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private int selectedEventId = -1;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        cbStatus.setItems(FXCollections.observableArrayList("upcoming", "ongoing", "completed", "cancelled"));

        formModifier.setDisable(true);
        loadEvents();

        tvEvents.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });
    }

    private void loadEvents() {
        try {
            ObservableList<Event> eventList = FXCollections.observableArrayList(serviceEvent.display());
            tvEvents.setItems(eventList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillForm(Event event) {
        formModifier.setDisable(false);
        selectedEventId = event.getId();
        tfTitle.setText(event.getTitle());
        taDescription.setText(event.getDescription());
        tfLocation.setText(event.getLocation());
        cbStatus.setValue(event.getStatus());
        tfClubId.setText(String.valueOf(event.getClubId()));

        if (event.getEventDate() != null) {
            dpEventDate.setValue(event.getEventDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
    }

    @FXML
    private void updateEvent(ActionEvent ev) {
        if (selectedEventId == -1) return;

        try {
            Event event = new Event();
            event.setId(selectedEventId);
            event.setTitle(tfTitle.getText().trim());
            event.setDescription(taDescription.getText().trim());
            event.setLocation(tfLocation.getText().trim());
            event.setStatus(cbStatus.getValue());
            try { event.setClubId(Integer.parseInt(tfClubId.getText().trim())); } catch (Exception e) {}
            if (dpEventDate.getValue() != null) {
                event.setEventDate(Date.from(dpEventDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            serviceEvent.update(event);
            
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Événement modifié !");
            a.showAndWait();
            
            formModifier.setDisable(true);
            selectedEventId = -1;
            loadEvents();

        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Erreur SQL: " + e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EventMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
