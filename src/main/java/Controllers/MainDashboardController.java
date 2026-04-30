package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import java.io.IOException;

public class MainDashboardController {

    private static MainDashboardController instance;

    public static MainDashboardController getInstance() {
        return instance;
    }

    @FXML
    private StackPane contentArea;
    
    @FXML
    private Label pageTitleLabel;

    @FXML
    public void initialize() {
        instance = this;
        // Load default view (Home Dashboard)
        loadView("Home.fxml", "Tableau de Bord");
    }

    @FXML
    public void goToDash() {
        loadView("Home.fxml", "Tableau de Bord");
    }

    @FXML
    public void goToEvents() {
        loadView("AfficherEvent.fxml", "Gestion des Événements");
    }

    @FXML
    public void goToClubs() {
        loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    @FXML
    public void handleLogout() {
        // Handle logout logic, return to Login
        System.out.println("Logout clicked");
    }

    public void loadView(String fxmlFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFileName));
            Parent view = loader.load();
            loadViewFromParent(view, title);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading " + fxmlFileName);
        }
    }

    public void loadViewFromParent(Parent view, String title) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
        pageTitleLabel.setText(title);
    }
}
