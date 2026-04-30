package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    public void goToEventMenu(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Événements");
    }

    public void goToClubMenu(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    public void quitApp(ActionEvent event) {
        System.exit(0);
    }


}
