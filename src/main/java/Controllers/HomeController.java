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
        navigate(event, "/EventMenu.fxml");
    }

    public void goToClubMenu(ActionEvent event) {
        navigate(event, "/ClubMenu.fxml");
    }

    public void quitApp(ActionEvent event) {
        System.exit(0);
    }

    private void navigate(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
