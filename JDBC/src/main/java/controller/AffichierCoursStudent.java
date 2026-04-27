package controller;

import Entities.Cours;
import Services.QRCodeService;
import Services.ServiceCours;
import Services.ServiceHistorique;
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
import javafx.stage.Modality;
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

        // --- 1. ZONE IMAGE AVEC ICÔNE QR ---
        StackPane imageStack = new StackPane();

        ImageView img = new ImageView();
        img.setFitWidth(280);
        img.setFitHeight(150);
        img.setPreserveRatio(false);
        img.setSmooth(true);

        // Chargement de l'image (ton code existant)
        String path = c.getImage();
        if (path != null && !path.isEmpty()) {
            try {
                String imageUri = (path.startsWith("file:/") || path.startsWith("http")) ? path : new File(path).toURI().toString();
                img.setImage(new Image(imageUri, true));
            } catch (Exception e) {
                System.err.println("Erreur image : " + e.getMessage());
            }
        }

        // --- AJOUT DE L'ICÔNE QR SUR L'IMAGE ---
        ImageView qrIcon = new ImageView();
        // On génère un mini QR visuel pour l'icône
        qrIcon.setImage(Services.QRCodeService.generateQRCode("SCAN", 100, 100));
        qrIcon.setFitWidth(40);
        qrIcon.setFitHeight(40);
        qrIcon.setCursor(Cursor.HAND);
        qrIcon.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 0);");

        // Action : Cliquer sur l'icône ouvre la petite fenêtre
        qrIcon.setOnMouseClicked(e -> showQRCodePopup(c));

        imageStack.getChildren().addAll(img, qrIcon);
        StackPane.setAlignment(qrIcon, Pos.TOP_RIGHT);
        StackPane.setMargin(qrIcon, new Insets(10));

        // --- 2. CONTENEUR D'INFORMATIONS ---
        VBox info = new VBox(10);
        info.setPadding(new Insets(15));

        // Badges
        HBox badges = new HBox(8);
        Label lvlBadge = new Label(c.getLevel() != null ? c.getLevel() : "Général");
        lvlBadge.setStyle("-fx-background-color: #FEF9E7; -fx-text-fill: #F1C40F; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label catBadge = new Label(c.getCategory() != null ? c.getCategory() : "Cours");
        catBadge.setStyle("-fx-background-color: #F4ECF7; -fx-text-fill: #8E44AD; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 10px;");
        badges.getChildren().addAll(lvlBadge, catBadge);

        // Titre
        Label title = new Label(c.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 17px; -fx-text-fill: #2D3436;");
        title.setWrapText(true);
        title.setMinHeight(40);
        // --- DATE (Publiée en dessous de la description) ---
        String dateStr = (c.getCreated_at() != null) ? c.getCreated_at().toString() : "Date inconnue";
        Label dateLabel = new Label("📅 Publié le : " + dateStr);
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");


        // Bouton Commencer (ton code existant)
        Button btn = new Button("Commencer");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #3C3362; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold;");
        btn.setOnAction(event -> {try {

            ServiceHistorique sh = new ServiceHistorique();
            sh.ajouterAuHistorique(c.getId());
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



        info.getChildren().addAll(badges, title, btn);
        card.getChildren().addAll(imageStack, info);

        return card;
    }

    // --- MÉTHODE POUR LA PETITE FENÊTRE (POPUP) ---
    private void showQRCodePopup(Cours c) {
        Stage popupStage = new Stage();
        popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popupStage.setTitle("Détails QR - " + c.getTitle());

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-border-color: #3C3362; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Données à afficher lors du scan
        String qrData = "--- DÉTAILS DU COURS ---\n" +
                "🆔 ID : " + c.getId() + "\n" +
                "📌 Titre : " + c.getTitle() + "\n" +
                "🏷️ Catégorie : " + c.getCategory() + "\n" +
                "📅 Créé le : " + (c.getCreated_at() != null ? c.getCreated_at() : "N/A") + "\n" +
                "📖 Description : " + c.getDescrption();

        // Génération du gros QR Code
        ImageView bigQR = new ImageView(Services.QRCodeService.generateQRCode(qrData, 300, 300));

        Label label = new Label("Scannez pour voir les infos");
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #3C3362;");

        Button close = new Button("Fermer");
        close.setOnAction(e -> popupStage.close());

        root.getChildren().addAll(label, bigQR, close);
        popupStage.setScene(new Scene(root));
        popupStage.show();
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
    }

}