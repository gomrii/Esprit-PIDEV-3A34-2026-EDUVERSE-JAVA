package Controllers;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AffichierCoursEnseignant {

    @FXML private TextField searchField;
    @FXML private TableView<Cours> table;
    @FXML private TableColumn<Cours, Integer> id;
    @FXML private TableColumn<Cours, String> titre, image, level, category, status, descrption;
    @FXML private TableColumn<Cours, Date> date;
    @FXML private TableColumn<Cours, Void> actions;
    @FXML private ComboBox<String> statusFilter, levelFilter;

    private final ServiceCours sc = new ServiceCours();

    public void initialize() {
        // 1. Mapping Colonnes
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        titre.setCellValueFactory(new PropertyValueFactory<>("title"));
        level.setCellValueFactory(new PropertyValueFactory<>("level"));
        image.setCellValueFactory(new PropertyValueFactory<>("image"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        date.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        descrption.setCellValueFactory(new PropertyValueFactory<>("descrption"));

        // 2. Rendu des images
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
                        if (img.isError()) {
                            setGraphic(null);
                        } else {
                            imageView.setImage(img);
                            setGraphic(imageView);
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // 3. Style des statuts (Couleurs)
        setupStatusStyle();

        // 4. Boutons d'actions (Modif / Suppr)
        setupActionsColumn();

        // 5. Filtres
        statusFilter.getItems().addAll("Tous", "pending", "approuved", "refuse");
        levelFilter.getItems().addAll("Tous", "Débutant", "Intermédiaire", "Avancé");

        loadTable();

        // Écouteurs pour recherche et filtres
        searchField.textProperty().addListener((obs, old, nv) -> handleSearch());
        statusFilter.setOnAction(e -> handleSearch());
        levelFilter.setOnAction(e -> handleSearch());
    }

    private void loadTable() {
        try {
            table.setItems(FXCollections.observableArrayList(sc.display()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupStatusStyle() {
        status.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item.toUpperCase());
                    if (item.equalsIgnoreCase("approuved")) setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    else if (item.equalsIgnoreCase("pending")) setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupActionsColumn() {
        actions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModif = new Button("Modifier");
            private final Button btnSuppr = new Button("Supprimer");
            private final HBox container = new HBox(10, btnModif, btnSuppr);

            {
                btnModif.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                btnSuppr.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                container.setAlignment(Pos.CENTER);

                btnModif.setOnAction(e -> {
                    Cours c = getTableView().getItems().get(getIndex());
                    // On charge la vue de modification via le Dashboard
                    MainDashboardController.getInstance().loadView("ModifierCoursEnseignant.fxml", "Modifier le Cours");
                    // Note: Il faudra passer l'objet Cours 'c' au contrôleur cible si nécessaire
                });

                btnSuppr.setOnAction(e -> {
                    Cours c = getTableView().getItems().get(getIndex());
                    if (confirm("Suppression", "Voulez-vous supprimer ce cours ?")) {
                        try { sc.delete(c); loadTable(); } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        try {
            List<Cours> data = sc.display();
            ObservableList<Cours> filtered = FXCollections.observableArrayList(
                    data.stream().filter(c -> c.getTitle().toLowerCase().contains(query)).toList()
            );
            table.setItems(filtered);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML void resetFilters() {
        searchField.clear();
        statusFilter.setValue("Tous");
        levelFilter.setValue("Tous");
        loadTable();
    }

    // --- NAVIGATION HARMONISÉE AVEC LE DASHBOARD (SIDEBAR) ---

    @FXML
    void gotoadd(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AjouterCoursEnseignant.fxml", "Ajouter un Cours");
    }

    @FXML
    void gotochap(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherChapitre.fxml", "Gestion des Chapitres");
    }

    @FXML
    void goToDash(ActionEvent event) {
        MainDashboardController.getInstance().loadView(ControllerUtils.getRoleBasedDashboardFxml(), ControllerUtils.getRoleBasedDashboardTitle());
    }

    @FXML
    void goToClubs(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    @FXML
    void goToEvents(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Evenements");
    }

    @FXML
    void goToQuiz(ActionEvent event) {
        MainDashboardController.getInstance().loadView(ControllerUtils.getRoleBasedQuizFxml(), ControllerUtils.getRoleBasedQuizTitle());
    }

    @FXML
    void goToAffichierCoursEnseignant(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AffichierCoursEnseignant.fxml", "Mes Cours");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        MainDashboardController.getInstance().handleLogout();
    }

    private boolean confirm(String t, String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.YES, ButtonType.NO);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}