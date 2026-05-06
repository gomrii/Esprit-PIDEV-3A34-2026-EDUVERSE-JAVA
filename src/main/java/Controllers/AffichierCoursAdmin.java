package Controllers;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import Entities.Cours;
import Services.ServiceCours;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class AffichierCoursAdmin {

    @FXML private TextField searchField;
    @FXML private TableView<Cours> table;
    @FXML private TableColumn<Cours, Integer> id;
    @FXML private TableColumn<Cours, String> titre;
    @FXML private TableColumn<Cours, String> image;
    @FXML private TableColumn<Cours, String> level;
    @FXML private TableColumn<Cours, String> category;
    @FXML private TableColumn<Cours, String> descrption;
    @FXML private TableColumn<Cours, String> status;
    @FXML private TableColumn<Cours, Date> date;
    @FXML private TableColumn<Cours, Void> actions;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> categoryFilter;

    private final ServiceCours sc = new ServiceCours();

    public void initialize() {
        // 1. Mapper les colonnes
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        titre.setCellValueFactory(new PropertyValueFactory<>("title"));
        level.setCellValueFactory(new PropertyValueFactory<>("level"));
        image.setCellValueFactory(new PropertyValueFactory<>("image"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        date.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        descrption.setCellValueFactory(new PropertyValueFactory<>("descrption"));

        // Gestion de l'image dans la cellule
        image.setCellFactory(param -> new TableCell<Cours, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);
                if (empty || photoPath == null || photoPath.isBlank() || photoPath.equalsIgnoreCase("hello") || photoPath.equals("default.png")) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = new Image(photoPath, 80, 50, true, true, true);
                        if (img.isError()) setGraphic(null);
                        else {
                            imageView.setImage(img);
                            setGraphic(imageView);
                        }
                    } catch (Exception e) { setGraphic(null); }
                }
            }
        });

        loadTable();
        setupActionsColumn();
        setupSearchLogic();

        // Initialisation filtres
        statusFilter.getItems().addAll("Tous", "pending", "approuved", "refuse");
        levelFilter.getItems().addAll("Tous", "Débutant", "Intermédiaire", "Avancé");
        categoryFilter.getItems().addAll("Tous", "Développement", "Design", "Business", "Marketing");

        statusFilter.setValue("Tous");
        levelFilter.setValue("Tous");
        categoryFilter.setValue("Tous");

        statusFilter.setOnAction(e -> loadTable());
        levelFilter.setOnAction(e -> loadTable());
        categoryFilter.setOnAction(e -> loadTable());
    }

    @FXML
    private void loadTable() {
        new Thread(() -> {
            try {
                List<Cours> coursList = sc.display();
                ObservableList<Cours> obs = FXCollections.observableArrayList(coursList);
                javafx.application.Platform.runLater(() -> table.setItems(obs));
            } catch (SQLException e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void setupActionsColumn() {
        actions.setMinWidth(420);
        Callback<TableColumn<Cours, Void>, TableCell<Cours, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnApprouver = new Button("Approuver");
            private final Button btnRefuser = new Button("Refuser");
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox container = new HBox(10);

            {
                btnApprouver.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
                btnRefuser.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-cursor: hand;");
                btnModifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                container.setAlignment(Pos.CENTER);

                btnApprouver.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    if (confirmAction("Confirmation", "Approuver ce cours ?")) {
                        new Thread(() -> {
                            try {
                                sc.updateStatus(cours.getId(), "approuved");
                                sendSMS("Félicitations ! Votre cours '" + cours.getTitle() + "' est approuvé.");
                                javafx.application.Platform.runLater(() -> {
                                    cours.setStatus("approuved");
                                    getTableView().refresh();
                                    showAlert("Succès", "Cours approuvé et enseignant notifié !", Alert.AlertType.INFORMATION);
                                });
                            } catch (SQLException e) { e.printStackTrace(); }
                        }).start();
                    }
                });

                btnModifier.setOnAction(event -> {
                    // 1. Récupérer le cours sélectionné dans la ligne de la table
                    Cours coursSelectionne = getTableView().getItems().get(getIndex());

                    // 2. Utiliser loadViewWithData pour envoyer le cours au contrôleur
                    MainDashboardController.getInstance().loadViewWithData(
                            "ModifierCoursAdmin.fxml",
                            "Modifier le Cours",
                            (ModifierCoursAdmin controller) -> {
                                // Ici, on appelle la méthode setCours que nous avons créée dans ModifierCoursAdmin
                                controller.setCours(coursSelectionne);
                            }
                    );
                });

                btnSupprimer.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    if (confirmAction("Attention", "Supprimer définitivement ce cours ?")) {
                        try { sc.delete(cours); loadTable(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Cours cours = getTableView().getItems().get(getIndex());
                    container.getChildren().clear();
                    if (cours.getStatus() != null && cours.getStatus().equalsIgnoreCase("pending")) {
                        container.getChildren().addAll(btnApprouver, btnRefuser);
                    }
                    container.getChildren().addAll(btnModifier, btnSupprimer);
                    setGraphic(container);
                }
            }
        };
        actions.setCellFactory(cellFactory);
    }

    private void setupSearchLogic() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("[a-zA-Z0-9 ]*")) searchField.setText(oldValue);
        });
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) handleSearchAction();
        });
    }

    private void handleSearchAction() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) { loadTable(); return; }
        try {
            List<Cours> results = sc.search(query);
            table.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- NAVIGATION CORRIGÉE (GARDE LA SIDEBAR) ---

    @FXML void goToDash(ActionEvent event) {
        MainDashboardController.getInstance().loadView(Utils.Session.role.equals("ADMIN") ? "/DashboardAdmin.fxml" : "/DashboardEnseignant.fxml", "Tableau de Bord");
    }

    @FXML void goToClubs(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    @FXML void goToEvents(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Evenements");
    }

    @FXML void goToQuiz(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherQuiz.fxml", "Gestion des Quiz");
    }

    @FXML void goToDemandes(ActionEvent event) {
        MainDashboardController.getInstance().loadView("PendingRequests.fxml", "Validation des Demandes");
    }

    @FXML void handleLogout(ActionEvent event) {
        MainDashboardController.getInstance().handleLogout();
    }

    @FXML void gotoadd(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AjouterCoursAdmin.fxml", "Ajouter un Cours");
    }

    @FXML void gototcha(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherChapitre.fxml", "Gestion des Chapitres");
    }

    @FXML void resetFilters() {
        statusFilter.setValue("Tous");
        levelFilter.setValue("Tous");
        categoryFilter.setValue("Tous");
        searchField.clear();
        loadTable();
    }

    // --- UTILITAIRES ---

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void sendSMS(String text) {
        String ACCOUNT_SID = "AC563f90ef77c3a87ba6b4aa4b6e249774";
        String AUTH_TOKEN = "9a2a0b1d57e618b7d3b26260362aef67";
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message.creator(new PhoneNumber("+21696068308"), new PhoneNumber("+17754598634"), text).create();
        } catch (Exception e) { System.err.println("Erreur Twilio : " + e.getMessage()); }
    }
}