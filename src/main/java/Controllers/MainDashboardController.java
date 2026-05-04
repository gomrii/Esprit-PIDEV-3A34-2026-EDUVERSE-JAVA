package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import Utils.Session;
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
    private Label roleLabel;

    @FXML
    public void initialize() {
        instance = this;
        
        updateRoleLabel();
        
        // Load default view (Home Dashboard)
        loadView("Home.fxml", "Tableau de Bord");
    }

    public void updateRoleLabel() {
        if (Session.role != null) {
            String roleText = Session.role;
            
            // Si c'est un étudiant ou enseignant, on vérifie s'il est créateur de club
            if (!"ADMIN".equals(Session.role)) {
                try {
                    Services.ServiceClub sc = new Services.ServiceClub();
                    if (!sc.getClubsByCreator(Session.userId).isEmpty()) {
                        roleText = "Créateur (" + Session.role + ")";
                    }
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
            }
            
            roleLabel.setText(roleText + " Session");
        }
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
    public void goToDemandes() {
        loadView("PendingRequests.fxml", "Validation des Demandes");
    }

    @FXML
    public void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/RoleSelection.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) roleLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
