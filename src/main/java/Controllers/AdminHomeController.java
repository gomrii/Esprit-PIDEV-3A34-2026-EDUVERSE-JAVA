package Controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class AdminHomeController {

    @FXML
    private void openQuizList(ActionEvent event) {
        open(event, "/admin_quiz_list.fxml");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        open(event, "/Home.fxml");
    }

    private void open(ActionEvent event, String fxml) {
        ControllerUtils.navigateTo((Node) event.getSource(), fxml, "Impossible d'ouvrir la page admin.");
    }
}
