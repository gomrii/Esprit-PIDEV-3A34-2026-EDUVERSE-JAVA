package Controllers;

import Utils.Session;
import com.elearning.entity.User;
import com.elearning.util.WindowHelper;
import com.elearning.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainDashboardController {

    private static final String DEFAULT_TITLE = "Tableau de bord";
    private static final String LOGIN_CSS = "/com/elearning/css/style.css";
    private static MainDashboardController instance;

    @FXML private BorderPane mainPane;
    @FXML private Label pageTitleLabel;
    @FXML private Label roleLabel;
    @FXML private Label sidebarUserLabel;
    @FXML private VBox primaryMenuBox;
    @FXML private VBox secondaryMenuBox;
    @FXML private ImageView logoView;
    @FXML private VBox sidebarTop;

    private final List<Button> menuButtons = new ArrayList<>();

    @FunctionalInterface
    public interface DataInitializer<T> {
        void init(T controller);
    }
    public static MainDashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        loadLogo();
        updateRoleLabel();
        configureRoleMenus();
        Platform.runLater(() -> {
            if (sidebarTop != null && sidebarTop.getScene() != null) {
                WindowHelper.enableWindowDrag(sidebarTop, (javafx.stage.Stage) sidebarTop.getScene().getWindow());
            }
        });
        goToDash();
    }

    private void loadLogo() {
        if (logoView == null) {
            return;
        }

        var resource = getClass().getResource("/com/elearning/images/logo.png");
        if (resource != null) {
            logoView.setImage(new Image(resource.toExternalForm()));
        }
    }

    private void updateSessionLabels() {
        User user = SessionManager.getInstance().getUtilisateurConnecte();
        String currentRole = formatRole(ControllerUtils.getCurrentRole());
        String currentUser = user != null && user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName()
                : "Utilisateur";

        roleLabel.setText(currentRole);
        sidebarUserLabel.setText(currentUser);
    }

    public void updateRoleLabel() {
        updateSessionLabels();
    }

    private void configureRoleMenus() {
        primaryMenuBox.getChildren().clear();
        secondaryMenuBox.getChildren().clear();
        menuButtons.clear();

        List<MenuEntry> primaryEntries = new ArrayList<>();
        List<MenuEntry> secondaryEntries = new ArrayList<>();

        primaryEntries.add(new MenuEntry("dashboard", "Dashboard", this::goToDash));
        primaryEntries.add(new MenuEntry("formations", "Formations", this::goToFormations));
        primaryEntries.add(new MenuEntry("quiz", "Quiz", this::goToQuiz));
        primaryEntries.add(new MenuEntry("cours", "Cours", this::goToCours));

        if (ControllerUtils.isAdminRole()) {
            secondaryEntries.add(new MenuEntry("demandes", "Demandes", this::goToDemandes));
            secondaryEntries.add(new MenuEntry("clubs", "Clubs", this::goToClubs));
            secondaryEntries.add(new MenuEntry("events", "Evenements", this::goToEvents));
        } else if (ControllerUtils.isTeacherRole()) {
            secondaryEntries.add(new MenuEntry("events", "Evenements", this::goToEvents));
            secondaryEntries.add(new MenuEntry("clubs", "Clubs", this::goToClubs));
        } else {
            secondaryEntries.add(new MenuEntry("events", "Evenements", this::goToEvents));
            secondaryEntries.add(new MenuEntry("clubs", "Clubs", this::goToClubs));
        }

        primaryEntries.forEach(entry -> primaryMenuBox.getChildren().add(createMenuButton(entry)));
        secondaryEntries.forEach(entry -> secondaryMenuBox.getChildren().add(createMenuButton(entry)));
    }

    private Button createMenuButton(MenuEntry entry) {
        Button button = new Button(entry.label());
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("menu-button");
        button.setOnAction(event -> {
            entry.action().run();
            setActiveMenu(entry.routeKey());
        });
        VBox.setVgrow(button, Priority.NEVER);
        menuButtons.add(button);
        button.getProperties().put("routeKey", entry.routeKey());
        return button;
    }

    private void setActiveMenu(String routeKey) {
        for (Button button : menuButtons) {
            Object key = button.getProperties().get("routeKey");
            boolean active = Objects.equals(routeKey, key);
            if (active) {
                if (!button.getStyleClass().contains("menu-button-active")) {
                    button.getStyleClass().add("menu-button-active");
                }
            } else {
                button.getStyleClass().remove("menu-button-active");
            }
        }
    }

    public void loadPage(String fxmlPath) {
        loadPage(fxmlPath, ControllerUtils.getRoleBasedDashboardTitle());
    }

    public void loadPage(String fxmlPath, String title) {
        try {
            String normalizedPath = normalizeFxmlPath(fxmlPath);
            Parent view = ControllerUtils.loadFxml(normalizedPath);
            loadViewFromParent(view, title);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de charger la vue " + fxmlPath + " : " + e.getMessage());
        }
    }

    public void loadView(String fxmlFileName, String title) {
        loadPage(fxmlFileName, title);
    }

    public <T> void loadViewWithData(String fxmlPath, String title, DataInitializer<T> initializer) {
        try {
            FXMLLoader loader = ControllerUtils.createLoader(normalizeFxmlPath(fxmlPath));
            Parent view = loader.load();
            T controller = loader.getController();
            if (initializer != null) {
                initializer.init(controller);
            }
            loadViewFromParent(view, title);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de charger la vue " + fxmlPath + " : " + e.getMessage());
        }
    }

    public void loadViewFromParent(Parent view, String title) {
        Parent preparedView = ControllerUtils.prepareEmbeddedRoot(view);
        BorderPane.setMargin(preparedView, new javafx.geometry.Insets(0, 28, 28, 28));
        mainPane.setCenter(preparedView);
        updatePageTitle(title);
    }

    @FXML
    public void goToDash() {
        setActiveMenu("dashboard");
        loadPage(ControllerUtils.getRoleBasedDashboardFxml(), ControllerUtils.getRoleBasedDashboardTitle());
    }

    @FXML
    public void goToFormations() {
        setActiveMenu("formations");
        loadPage("/com/elearning/gui/FormationDashboardView.fxml", "Gestion des Formations");
    }

    @FXML
    public void goToEvents() {
        setActiveMenu("events");
        loadPage("/AfficherEvent.fxml", "Gestion des Evenements");
    }

    @FXML
    public void goToClubs() {
        setActiveMenu("clubs");
        loadPage("/AfficherClub.fxml", "Gestion des Clubs");
    }

    @FXML
    public void goToDemandes() {
        if (!ControllerUtils.isAdminRole()) {
            ControllerUtils.showError("La page de validation des demandes est reservee a l'administration.");
            return;
        }
        setActiveMenu("demandes");
        loadPage("/PendingRequests.fxml", "Validation des Demandes");
    }

    @FXML
    public void goToQuiz() {
        setActiveMenu("quiz");
        loadPage(ControllerUtils.getRoleBasedQuizFxml(), ControllerUtils.getRoleBasedQuizTitle());
    }

    @FXML
    public void goToCours() {
        setActiveMenu("cours");
        loadPage(ControllerUtils.getRoleBasedCoursFxml(), ControllerUtils.getRoleBasedCoursTitle());
    }

    @FXML
    public void handleLogout() {
        Session.clear();
        SessionManager.getInstance().deconnecter();
        try {
            Parent root = ControllerUtils.loadFxml("/com/elearning/gui/LoginView.fxml");
            javafx.stage.Stage stage = (javafx.stage.Stage) roleLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            attachStylesheet(scene);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de revenir a la page de connexion : " + e.getMessage());
        }
    }

    public boolean isAttachedToScene(Scene scene) {
        return mainPane != null && mainPane.getScene() != null && mainPane.getScene() == scene;
    }

    public String getCurrentPageTitle() {
        return pageTitleLabel != null ? pageTitleLabel.getText() : DEFAULT_TITLE;
    }

    private void updatePageTitle(String title) {
        if (pageTitleLabel != null) {
            pageTitleLabel.setText(title == null || title.isBlank() ? DEFAULT_TITLE : title);
        }
    }

    private String normalizeFxmlPath(String fxmlPath) {
        return fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
    }

    private void attachStylesheet(Scene scene) {
        var cssResource = getClass().getResource(LOGIN_CSS);
        if (cssResource == null) {
            throw new IllegalStateException("Feuille CSS introuvable: " + LOGIN_CSS);
        }
        String css = cssResource.toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    private String formatRole(String role) {
        if (role == null || role.isBlank()) {
            return "Session";
        }
        return switch (role.toUpperCase()) {
            case User.ROLE_ADMIN -> "Espace Admin";
            case User.ROLE_ENSEIGNANT -> "Espace Enseignant";
            case User.ROLE_ETUDIANT -> "Espace Etudiant";
            default -> role;
        };
    }

    private record MenuEntry(String routeKey, String label, Runnable action) {
    }
}
