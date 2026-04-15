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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class SupprimerEventController {

    @FXML private TableView<Event> tvEvents;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, Date> colDate;

    private final ServiceEvent serviceEvent = new ServiceEvent();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        loadEvents();
    }

    private void loadEvents() {
        try {
            ObservableList<Event> eventList = FXCollections.observableArrayList(serviceEvent.display());
            tvEvents.setItems(eventList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteEvent() {
        Event selectedEvent = tvEvents.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un événement.");
            a.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    serviceEvent.delete(selectedEvent);
                    loadEvents(); 
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Événement supprimé !");
                    a.showAndWait();
                } catch (SQLException e) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Erreur SQL: " + e.getMessage());
                    a.showAndWait();
                }
            }
        });
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
