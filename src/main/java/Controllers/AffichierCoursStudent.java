package Controllers;

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
    @FXML
    void goToHistorique(ActionEvent event) {
        String fxmlPath = "/HistoriqueView.fxml"; // Vérifie bien le nom de ton fichier
        naviguerVers(fxmlPath, "Mon Historique", event);
    }

    @FXML
    void goToRecommandations(ActionEvent event) {
        String fxmlPath = "/Recommandation.fxml"; // Le fichier FXML que nous avons créé ensemble
        naviguerVers(fxmlPath, "Mes Recommandations IA", event);
    }

    // Méthode générique pour éviter la répétition de code
    private void naviguerVers(String path, String title, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + path + " : " + e.getMessage());
            e.printStackTrace();
        }
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

        // Arrondir les coins supérieurs de l'image pour coller à la carte
        Rectangle clip = new Rectangle(280, 150);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        img.setClip(clip);

        String path = c.getImage();
        if (path != null && !path.isEmpty()) {
            try {
                String imageUri = (path.startsWith("file:/") || path.startsWith("http")) ? path : new File(path).toURI().toString();
                img.setImage(new Image(imageUri, true));
            } catch (Exception e) {
                System.err.println("Erreur image : " + e.getMessage());
            }
        }

        ImageView qrIcon = new ImageView();
        qrIcon.setImage(Services.QRCodeService.generateQRCode("SCAN", 100, 100));
        qrIcon.setFitWidth(40);
        qrIcon.setFitHeight(40);
        qrIcon.setCursor(Cursor.HAND);
        qrIcon.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 0);");
        qrIcon.setOnMouseClicked(e -> showQRCodePopup(c));

        imageStack.getChildren().addAll(img, qrIcon);
        StackPane.setAlignment(qrIcon, Pos.TOP_RIGHT);
        StackPane.setMargin(qrIcon, new Insets(10));

        // --- 2. CONTENEUR D'INFORMATIONS ---
        VBox info = new VBox(10); // Espacement entre les éléments
        info.setPadding(new Insets(15));

        // Badges (Niveau & Catégorie)
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

        // --- DESCRIPTION ---
        String descText = (c.getDescrption() != null && !c.getDescrption().isEmpty())
                ? c.getDescrption()
                : "Aucune description disponible pour ce cours.";
        Label description = new Label(descText);
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: #636e72;");
        description.setWrapText(true);
        description.setMaxHeight(50); // Limite pour garder la carte compacte
        description.setEllipsisString("...");

        // --- DATE ---
        String dateStr = (c.getCreated_at() != null) ? c.getCreated_at().toString() : "Date inconnue";
        Label dateLabel = new Label("📅 Publié le : " + dateStr);
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");

        // Bouton Commencer
        Button btn = new Button("Commencer");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(Cursor.HAND);
        btn.setStyle("-fx-background-color: #3C3362; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold; -fx-padding: 8;");
        btn.setOnAction(event -> {
            try {
                ServiceHistorique sh = new ServiceHistorique();
                sh.ajouterAuHistorique(c.getId());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherChapitreStudent.fxml"));
                Parent root = loader.load();
                AfficherChapitreStudent controller = loader.getController();
                controller.setCoursData(c);
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Ajout de tous les éléments à la section "info" dans l'ordre
        info.getChildren().addAll(badges, title, description, dateLabel, btn);

        // Assemblage final
        card.getChildren().addAll(imageStack, info);

        return card;
    }

    private void showQRCodePopup(Cours c) {
        Stage popupStage = new Stage();
        popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popupStage.setTitle("QR YouTube - " + c.getTitle());

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        // Style mis à jour avec une bordure rouge "YouTube"
        root.setStyle("-fx-background-color: white; -fx-border-color: #FF0000; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");

        // --- PRÉPARATION DU LIEN YOUTUBE ---
        // On encode le titre pour l'URL (remplace les espaces par +)
        String searchQuery = c.getTitle().replace(" ", "+");
        String youtubeLink = "https://www.youtube.com/results?search_query=" + searchQuery;

        // --- DONNÉES DU QR CODE ---
        // Le QR code contient maintenant uniquement l'URL pour une ouverture automatique
        String qrData = youtubeLink;

        // Génération du QR Code
        ImageView bigQR = new ImageView(Services.QRCodeService.generateQRCode(qrData, 300, 300));

        // --- INTERFACE ---
        Label label = new Label("Scannez pour chercher sur YouTube");
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF0000; -fx-font-size: 14px;");

        Label infoLien = new Label("Recherche : " + c.getTitle());
        infoLien.setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic;");

        Button close = new Button("Fermer");
        close.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        close.setOnAction(e -> popupStage.close());

        root.getChildren().addAll(label, bigQR, infoLien, close);

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
    void goToAffichierCoursStudent(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AffichierCoursStudent.fxml", "Mes Cours");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        MainDashboardController.getInstance().handleLogout();
    }}