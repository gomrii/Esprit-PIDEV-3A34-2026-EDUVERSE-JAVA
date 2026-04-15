package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClubMenuController {

    // Les vues Club ne sont pas encore créées, cette classe sert d'exemple de structure
    public void goToAjouterClub(ActionEvent event) { navigate(event, "/AjouterClub.fxml"); }
    public void goToAfficherClub(ActionEvent event) { navigate(event, "/AfficherClub.fxml"); }
    public void goToModifierClub(ActionEvent event) { navigate(event, "/ModifierClub.fxml"); }
    public void goToSupprimerClub(ActionEvent event) { navigate(event, "/SupprimerClub.fxml"); }
    
    public void goBackHome(ActionEvent event) { navigate(event, "/Home.fxml"); }

    private void navigate(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Fichier FXML non trouvé si pas encore créé
        }
    }
}
