package controller;

import Entities.Cours;
import Services.ServiceRecommandation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class RecommandationController {

    @FXML private VBox containerCards, listeHistorique;
    @FXML private Label lblNomCandidat, lblStatsCours, lblPercent;
    @FXML private ProgressBar progressGlobal;

    private ServiceRecommandation service = new ServiceRecommandation();

    @FXML
    public void initialize() {
        refreshUI();
    }

    private void refreshUI() {
        containerCards.getChildren().clear();
        listeHistorique.getChildren().clear();

        // Données réelles
        List<String> historique = service.getListeTitresHistorique();
        List<Cours> suggestions = service.genererSuggestionsML();

        // 1. Mise à jour Profil
        lblNomCandidat.setText("Sahar");
        int totalC = 10; // Exemple
        int suivis = historique.size();
        lblStatsCours.setText("Cours suivis: " + suivis + " / " + totalC);

        double ratio = (double) suivis / totalC;
        progressGlobal.setProgress(ratio);
        lblPercent.setText((int)(ratio * 100) + "%");

        // 2. Remplissage Sidebar
        for (String titre : historique) {
            Label hLabel = new Label("✓ " + titre);
            hLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 13; -fx-font-weight: bold;");
            listeHistorique.getChildren().add(hLabel);
        }

        // 3. Génération des Cartes Dynamiques
        if (suggestions.isEmpty()) {
            containerCards.getChildren().add(new Label("Revenez plus tard pour de nouvelles suggestions !"));
        } else {
            for (Cours c : suggestions) {
                containerCards.getChildren().add(creerCarteCours(c));
            }
        }
    }

    private HBox creerCarteCours(Cours c) {
        HBox card = new HBox(25);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 18; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 5);");

        // Infos (Titre, Badge, Catégorie)
        VBox info = new VBox(10);
        Label title = new Label(c.getTitle());
        title.setStyle("-fx-font-size: 19; -fx-font-weight: bold; -fx-text-fill: #2D1B4E;");

        Label badge = new Label(c.getLevel().toUpperCase());
        badge.setStyle("-fx-background-color: #FEF5E7; -fx-text-fill: #F39C12; -fx-padding: 5 12; " +
                "-fx-background-radius: 12; -fx-font-size: 10; -fx-font-weight: bold;");

        Label desc = new Label("Domaine : " + c.getCategory());
        desc.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 13;");

        info.getChildren().addAll(title, badge, desc);
        HBox.setHgrow(info, Priority.ALWAYS);

        // On n'ajoute que la VBox info à la HBox card
        card.getChildren().addAll(info);

        return card;
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AffichierCoursStudent.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
}