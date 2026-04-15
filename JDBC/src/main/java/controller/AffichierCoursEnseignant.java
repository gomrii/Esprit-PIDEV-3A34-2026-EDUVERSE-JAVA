package controller;

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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AffichierCoursEnseignant {

    @FXML private TextField searchField;
    @FXML private TableView<Cours> table;
    @FXML private TableColumn<Cours, Integer> id;
    @FXML private TableColumn<Cours, String> titre, image, level, category, status,descrption;
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
        image.setCellFactory(param -> new TableCell<Cours, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);

                // Si la cellule est vide ou si le chemin est invalide (comme "hello" ou "default.png")
                if (empty || photoPath == null || photoPath.isBlank() || photoPath.equalsIgnoreCase("hello") || photoPath.equals("default.png")) {
                    setGraphic(null);
                } else {
                    try {
                        // On tente de charger l'image
                        Image img = new Image(photoPath, 80, 50, true, true, true);

                        // Si l'image charge avec une erreur (fichier supprimé du disque par ex)
                        if (img.isError()) {
                            setGraphic(null); // On n'affiche rien du tout
                        } else {
                            imageView.setImage(img);
                            setGraphic(imageView);
                        }
                    } catch (Exception e) {
                        // En cas d'exception, on sécurise en n'affichant rien
                        setGraphic(null);
                    }
                }
            }
        });

        // 2. Rendu des images


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
                    goToModifier(c, e);
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
        // Logique de recherche filtrée
        try {
            List<Cours> data = sc.display();
            // Filtrage manuel pour l'exemple (ou via ta méthode sc.search)
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


    @FXML void gotoadd(ActionEvent event) { navigate(event, "/AjouterCoursEnseignant.fxml"); }

    private void goToModifier(Cours c, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCoursEnseignant.fxml"));
            Parent root = loader.load();
            ModifierCoursEnseignant ctrl = loader.getController();
            ctrl.setCours(c);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean confirm(String t, String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.YES, ButtonType.NO);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
    @FXML
    void gotochap(ActionEvent event) {
        navigate(event, "/AfficherChapitre.fxml");
    }
    @FXML void goToMenu(ActionEvent event) { navigate(event, "/MainMenu.fxml"); }

}