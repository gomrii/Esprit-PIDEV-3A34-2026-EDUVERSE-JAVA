package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EventMenuController {

    public void goToAjouterEvent(ActionEvent event) { navigate(event, "/AjouterEvent.fxml"); }
    public void goToAfficherEvent(ActionEvent event) { navigate(event, "/AfficherEvent.fxml"); }
    public void goToModifierEvent(ActionEvent event) { navigate(event, "/ModifierEvent.fxml"); }
    public void goToSupprimerEvent(ActionEvent event) { navigate(event, "/SupprimerEvent.fxml"); }
    
    public void goBackHome(ActionEvent event) { navigate(event, "/Home.fxml"); }

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
