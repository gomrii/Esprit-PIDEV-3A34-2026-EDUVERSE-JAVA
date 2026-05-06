package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class DashboardController {

    @FXML
    void goBack(ActionEvent event) {
        ControllerUtils.navigateTo((Node) event.getSource(), "/MainDashboard.fxml", "Impossible de retourner au dashboard.");
    }

    @FXML
    public void goToDash() {
        MainDashboardController.getInstance().loadView(ControllerUtils.getRoleBasedDashboardFxml(), ControllerUtils.getRoleBasedDashboardTitle());
    }

    @FXML
    public void goToEvents() {
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Evenements");
    }

    @FXML
    public void goToClubs() {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    @FXML
    public void goToQuiz() {
        MainDashboardController.getInstance().loadView(ControllerUtils.getRoleBasedQuizFxml(), ControllerUtils.getRoleBasedQuizTitle());
    }

    @FXML
    public void goToAffichierCoursAdmin() {
        MainDashboardController.getInstance().loadView("AffichierCoursAdmin.fxml", "Gestion des Cours");
    }

    @FXML
    public void goToDemandes() {
        MainDashboardController.getInstance().loadView("PendingRequests.fxml", "Validation des Demandes");
    }

    @FXML
    public void handleLogout() {
        MainDashboardController.getInstance().handleLogout();
    }
}
