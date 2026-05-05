package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class DashboardController {

    @FXML
    void goBack(ActionEvent event) {
        ControllerUtils.navigateTo((Node) event.getSource(), "/MainDashboard.fxml", "Impossible de retourner au dashboard.");
    }
}
