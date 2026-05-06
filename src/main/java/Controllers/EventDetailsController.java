package Controllers;

import Entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.text.SimpleDateFormat;

public class EventDetailsController {

    @FXML private Label eventTitleLabel;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;

    public void setEvent(Event event) {
        eventTitleLabel.setText(event.getTitle());
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
        dateLabel.setText(event.getEventDate() != null ? formatter.format(event.getEventDate()) : "N/A");
        
        locationLabel.setText(event.getLocation());
        statusLabel.setText(event.getStatus());
        descriptionLabel.setText(event.getDescription());
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Événements");
    }
}
