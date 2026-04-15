package controller;

import Entities.Cours;
import Services.ServiceCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AffichierCoursStudent {

    @FXML private FlowPane container;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> levelFilter;
    @FXML private Label nbCoursLabel;

    private List<Cours> allCours = new ArrayList<>();
    private final ServiceCours service = new ServiceCours();

    @FXML
    public void initialize() {
        if (levelFilter == null || container == null) return;

        // Configuration du filtre de niveau
        levelFilter.getItems().setAll("Tous", "Débutant", "Intermédiaire", "Avancé");
        levelFilter.setValue("Tous");

        loadData();

        // Écouteurs pour la recherche et le filtre
        searchField.textProperty().addListener((obs, old, nv) -> filterData());
        levelFilter.valueProperty().addListener((obs, old, nv) -> filterData());
    }

    private void loadData() {
        try {
            // Récupération et filtrage des cours approuvés
            allCours = service.display().stream()
                    .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase("approuved"))
                    .collect(Collectors.toList());
            filterData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterData() {
        String query = searchField.getText().toLowerCase();
        String lvl = levelFilter.getValue();

        List<Cours> filtered = allCours.stream()
                .filter(c -> c.getTitle().toLowerCase().contains(query))
                .filter(c -> lvl.equals("Tous") || (c.getLevel() != null && c.getLevel().equalsIgnoreCase(lvl)))
                .collect(Collectors.toList());

        updateDisplay(filtered);
    }

    private void updateDisplay(List<Cours> list) {
        container.getChildren().clear();
        for (Cours c : list) {
            container.getChildren().add(createCourseCard(c));
        }
        if (nbCoursLabel != null) nbCoursLabel.setText(list.size() + " cours disponibles");
    }

    private VBox createCourseCard(Cours c) {
        VBox card = new VBox(0);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        // Ombre pour le design
        DropShadow ds = new DropShadow(15, Color.rgb(0, 0, 0, 0.1));
        ds.setOffsetY(5);
        card.setEffect(ds);
         
        // 1. Image du cours
        // 1. Image du cours
        ImageView img = new ImageView();
        img.setFitWidth(280);
        img.setFitHeight(150);
        img.setPreserveRatio(true);

        boolean imageLoaded = false;
        String path = c.getImage();

        if (path != null && !path.isEmpty()) {
            try {
                // Vérifie si c'est déjà une URI ou un chemin brut
                String imageUri;
                if (path.startsWith("file:/") || path.startsWith("http")) {
                    imageUri = path;
                } else {
                    imageUri = new File(path).toURI().toString();
                }

                Image loadedImage = new Image(imageUri, true); // true pour chargement asynchrone

                // On attend que l'image soit chargée pour vérifier les erreurs
                loadedImage.errorProperty().addListener((obs, old, hasError) -> {
                    if (hasError) {
                        // Si erreur, on peut mettre une image par défaut ici
                        System.err.println("Erreur de chargement pour : " + path);
                    }
                });

                img.setImage(loadedImage);
                imageLoaded = true;

            } catch (Exception e) {
                System.err.println("Erreur lors du traitement du chemin : " + e.getMessage());
            }
        }

// Si pas d'image chargée, on peut soit laisser vide, soit charger une ressource interne
        if (!imageLoaded) {
            // Optionnel : img.setImage(new Image(getClass().getResourceAsStream("/default-course.png")));
        }
        // Dans createCourseCard
        img.setPreserveRatio(false); // On force le remplissage du rectangle 280x150
        img.setSmooth(true);

        // 2. Conteneur d'informations
        VBox info = new VBox(10);
        info.setPadding(new Insets(15));

        // --- BADGES (Niveau & Catégorie) ---
        HBox badges = new HBox(8);

        Label lvlBadge = new Label(c.getLevel() != null ? c.getLevel() : "Général");
        lvlBadge.setStyle("-fx-background-color: #FEF9E7; -fx-text-fill: #F1C40F; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label catBadge = new Label(c.getCategory() != null ? c.getCategory() : "Cours");
        catBadge.setStyle("-fx-background-color: #F4ECF7; -fx-text-fill: #8E44AD; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 10px;");

        badges.getChildren().addAll(lvlBadge, catBadge);

        // --- TITRE ---
        Label title = new Label(c.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #2D3436;");
        title.setWrapText(true);
        title.setMinHeight(40);

        // --- DESCRIPTION ---
        Label desc = new Label(c.getDescrption()); // Respect de l'orthographe de ton entité
        desc.setStyle("-fx-text-fill: #636E72; -fx-font-size: 12px;");
        desc.setWrapText(true);
        desc.setPrefHeight(50);
        desc.setAlignment(Pos.TOP_LEFT);

        // --- DATE (Publiée en dessous de la description) ---
        String dateStr = (c.getCreated_at() != null) ? c.getCreated_at().toString() : "Date inconnue";
        Label dateLabel = new Label("📅 Publié le : " + dateStr);
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");

        // --- BOUTON ---
        Button btn = new Button("Commencer");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(35);
        btn.setCursor(Cursor.HAND);
        btn.setStyle("-fx-background-color: #3C3362; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold;");

// --- LIAISON ---
        btn.setOnAction(event -> {
            try {
                // 1. Charger le fichier FXML de la page des chapitres
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherChapitreStudent.fxml"));
                Parent root = loader.load();

                // 2. Récupérer le contrôleur de la page de destination
                AfficherChapitreStudent controller = loader.getController();

                // 3. Passer les données du cours actuel (l'objet 'c' ou 'cours')
                controller.setCoursData(c);

                // 4. Changer la scène
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                System.err.println("Erreur de chargement : " + e.getMessage());
                e.printStackTrace();
            }
        });

// Assemblage final
        info.getChildren().addAll(badges, title, desc, dateLabel, btn);
        card.getChildren().addAll(img, info);

        return card;
    }

    @FXML
    void handleSearch(ActionEvent event) {
        filterData();
    }


    @FXML
    void gotomenu(ActionEvent event) {
        String fxmlPath = "/MainMenu.fxml";
        String title = "Administration des Cours";

        try {
            // Chargement de la vue
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Récupération de la fenêtre (Stage) depuis l'événement
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Configuration de la fenêtre
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen(); // Utile pour les grandes interfaces Admin
            stage.show();

        } catch (IOException e) {
            // Gestion d'erreur conforme à ton modèle
            System.err.println("Erreur de chargement du FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }}