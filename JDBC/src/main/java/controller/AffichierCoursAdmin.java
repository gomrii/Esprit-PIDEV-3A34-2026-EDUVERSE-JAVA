package controller;

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

        image.setCellFactory(param -> new TableCell<Cours, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);
                if (empty || photoPath == null || photoPath.isEmpty() || photoPath.equalsIgnoreCase("hello")) {
                    setGraphic(null);
                } else {
                    try {
                        java.io.File file = new java.io.File(photoPath.replace("\\", "/"));
                        if (file.exists()) {
                            Image img = new Image(file.toURI().toString());
                            imageView.setImage(img);
                            imageView.setFitWidth(80);
                            imageView.setFitHeight(50);
                            imageView.setPreserveRatio(true);
                            setGraphic(imageView);
                        } else { setGraphic(null); }
                    } catch (Exception e) { setGraphic(null); }
                }
            }
        });

        loadTable();
        setupActionsColumn();
        setupSearchLogic();

        statusFilter.getItems().addAll("Tous", "pending", "approuved", "refuse");
        levelFilter.getItems().addAll("Tous", "Débutant", "Intermédiaire", "Avancé");
        categoryFilter.getItems().addAll("Tous", "Développement", "Design", "Business", "Marketing");

        statusFilter.setValue("Tous");
        levelFilter.setValue("Tous");
        categoryFilter.setValue("Tous");

        statusFilter.setOnAction(e -> loadTable());
        levelFilter.setOnAction(e -> loadTable());
        categoryFilter.setOnAction(e -> loadTable());
        searchField.setOnAction(e -> loadTable());
    }

    @FXML
    private void loadTable() {
        new Thread(() -> {
            try {
                List<Cours> coursList = sc.display();
                ObservableList<Cours> obs = FXCollections.observableArrayList(coursList);
                javafx.application.Platform.runLater(() -> table.setItems(obs));
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
                        btnApprouver.setDisable(true);
                        new Thread(() -> {
                            try {
                                sc.updateStatus(cours.getId(), "approuved");
                                sendSMS("Félicitations ! Votre cours '" + cours.getTitle() + "' est approuvé.");
                                javafx.application.Platform.runLater(() -> {
                                    cours.setStatus("approuved");
                                    getTableView().refresh();
                                    btnApprouver.setDisable(false);
                                    showAlert("Succès", "Cours approuvé et enseignant notifié !", Alert.AlertType.INFORMATION);
                                });
                            } catch (SQLException e) {
                                javafx.application.Platform.runLater(() -> {
                                    btnApprouver.setDisable(false);
                                    showAlert("Erreur", "Problème BDD", Alert.AlertType.ERROR);
                                });
                            }
                        }).start();
                    }
                });

                btnRefuser.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    if (confirmAction("Confirmation", "Refuser ce cours ?")) {
                        try {
                            sc.updateStatus(cours.getId(), "refuse");
                            cours.setStatus("refuse");
                            getTableView().refresh();
                            showAlert("Mise à jour", "Le cours a été refusé.", Alert.AlertType.INFORMATION);
                        } catch (SQLException e) { e.printStackTrace(); }
                    }
                });

                btnModifier.setOnAction(event -> {
                    try {
                        Cours cours = getTableView().getItems().get(getIndex());
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCoursAdmin.fxml"));
                        Parent root = loader.load();
                        ModifierCoursAdmin controller = loader.getController();
                        controller.setCours(cours);
                        Stage stage = (Stage) btnModifier.getScene().getWindow();
                        stage.setScene(new Scene(root));
                    } catch (Exception e) { e.printStackTrace(); }
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

    // --- LOGIQUE DE RECHERCHE ---
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

    // --- NAVIGATION ---
    @FXML
    void goToMenu(ActionEvent event) {
        navigate(event, "/MainMenu.fxml");
    }

    @FXML
    void goToStats(ActionEvent event) {
        navigate(event, "/StatistiqueAdmin.fxml");
    }

    @FXML
    void gotoadd(ActionEvent event) {
        navigate(event, "/AjouterCoursAdmin.fxml");
    }
    @FXML
    void gototcha(ActionEvent event) {navigate(event, "/AfficherChapitre.fxml");

    }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- UTILITAIRES ---
    private void sendSMS(String text) {
        String ACCOUNT_SID = "AC563f90ef77c3a87ba6b4aa4b6e249774";
        String AUTH_TOKEN = "b632734b438492fd3e23173cdd0dfef0";
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message.creator(new PhoneNumber("+21696068308"), new PhoneNumber("+17754598634"), text).create();
        } catch (Exception e) { System.err.println("Erreur Twilio : " + e.getMessage()); }
    }

    @FXML
    private void resetFilters() {
        statusFilter.setValue("Tous");
        levelFilter.setValue("Tous");
        categoryFilter.setValue("Tous");
        searchField.clear();
        loadTable();
    }

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
}