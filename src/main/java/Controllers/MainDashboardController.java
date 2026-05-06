package Controllers;

import Utils.Session;
import com.elearning.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainDashboardController {

    private static MainDashboardController instance;
    @FXML private Label viewTitleLabel; // Ajoute cette ligne

    public static MainDashboardController getInstance() {
        return instance;
    }

    @FXML
    private StackPane contentArea;

    // À mettre à l'intérieur de la classe MainDashboardController
    @FunctionalInterface
    public interface DataInitializer<T> {
        void init(T controller);
    }
    @FXML
    private Label pageTitleLabel;


    @FXML
    private Label roleLabel;

    @FXML
    private Label menuDemandes;

    @FXML
    private Label menuQuiz;


    @FXML
    public void initialize() {
        instance = this;
        updateRoleLabel();
        configureRoleMenus();
        loadView(ControllerUtils.getRoleBasedDashboardFxml(), ControllerUtils.getRoleBasedDashboardTitle());
    }

    private void configureRoleMenus() {
        if (menuDemandes != null && !"ADMIN".equals(Session.role)) {
            menuDemandes.setVisible(false);
            menuDemandes.setManaged(false);
        }
    }


    public void updateRoleLabel() {
        if (Session.role == null) {
            roleLabel.setText("Session");
            return;
        }

        String roleText = Session.role;
        if (!"ADMIN".equals(Session.role)) {
            try {
                Services.ServiceClub sc = new Services.ServiceClub();
                if (!sc.getClubsByCreator(Session.userId).isEmpty()) {
                    roleText = "Createur (" + Session.role + ")";
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }

        roleLabel.setText(roleText + " Session");
    }

    @FXML
    public void goToDash() {
        loadView(ControllerUtils.getRoleBasedDashboardFxml(), ControllerUtils.getRoleBasedDashboardTitle());
    }

    @FXML
    public void goToFormations() {
        loadView("/com/elearning/gui/FormationDashboardView.fxml", "Gestion des Formations");
    }

    @FXML
    public void goToEvents() {
        loadView("AfficherEvent.fxml", "Gestion des Evenements");
    }

    @FXML
    public void goToClubs() {
        loadView("AfficherClub.fxml", "Gestion des Clubs");
    }


    @FXML
    public void goToDemandes() {
        if (!"ADMIN".equals(Session.role)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Acces refuse");
            alert.setHeaderText(null);
            alert.setContentText("La page de validation des demandes est accessible uniquement depuis le tableau de bord administrateur.");
            alert.showAndWait();
            return;
        }
        loadView("PendingRequests.fxml", "Validation des Demandes");
    }

    @FXML
    public void goToQuiz() {
        loadView(ControllerUtils.getRoleBasedQuizFxml(), ControllerUtils.getRoleBasedQuizTitle());
    }

    @FXML
    public void goToAffichierCoursAdmin() {
        loadView("AffichierCoursAdmin.fxml", "Gestion des Cours");
    }

    @FXML
    public void goToAffichierCoursEnseignant() {
        loadView("AffichierCoursEnseignant.fxml", "Mes Cours");
    }

    @FXML
    public void goToAffichierCoursStudent() {
        loadView("AffichierCoursStudent.fxml", "Mes Cours");
    }

    @FXML
    public void goToCours() {
        loadView(ControllerUtils.getRoleBasedCoursFxml(), ControllerUtils.getRoleBasedCoursTitle());
    }

    @FXML
    public void handleLogout() {
        Session.clear();
        SessionManager.getInstance().deconnecter();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/elearning/gui/LoginView.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) roleLabel.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public <T> void loadViewWithData(String fxmlPath, String title, DataInitializer<T> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
            Parent root = loader.load();

            // 1. Mise à jour du titre (Si tu as ajouté la déclaration au point 1)
            if (viewTitleLabel != null) {
                viewTitleLabel.setText(title);
            }

            // 2. Initialisation des données
            T controller = loader.getController();
            if (initializer != null) {
                initializer.init(controller);
            }

            // 3. Affichage dans le conteneur principal (ContentArea)
            contentArea.getChildren().setAll(root);

        } catch (IOException e) {
            // Affiche une alerte détaillée
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur critique : Impossible de charger " + fxmlPath + "\n\n" + e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }

    public void loadView(String fxmlFileName, String title) {
        try {
            String normalizedPath = fxmlFileName.startsWith("/") ? fxmlFileName : "/" + fxmlFileName;
            Parent view = ControllerUtils.loadFxml(normalizedPath);
            loadViewFromParent(view, title);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de charger la vue " + fxmlFileName + " : " + e.getMessage());
        }
    }

    public void loadViewFromParent(Parent view, String title) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
        if (title != null && !title.isBlank()) {
            pageTitleLabel.setText(title);
        }
    }

    public boolean isAttachedToScene(Scene scene) {
        return contentArea != null && contentArea.getScene() != null && contentArea.getScene() == scene;
    }

    public String getCurrentPageTitle() {
        return pageTitleLabel != null ? pageTitleLabel.getText() : "EduVerse";
    }

    public void loadViewWithData(String s, String s1, Object o) {
    }
}
