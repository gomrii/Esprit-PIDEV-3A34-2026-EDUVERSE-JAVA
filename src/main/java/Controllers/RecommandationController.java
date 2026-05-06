package Controllers;

import Entities.Cours;
import Services.ServiceRecommandation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class RecommandationController {

    @FXML private VBox containerCards, listeHistorique;
    @FXML private Label lblNomCandidat, lblStatsCours, lblPercent;
    @FXML private ProgressBar progressGlobal;

    private final ServiceRecommandation service = new ServiceRecommandation();

    @FXML
    public void initialize() {
        refreshUI();
    }

    private void refreshUI() {
        // Nettoyage des anciens éléments
        containerCards.getChildren().clear();
        listeHistorique.getChildren().clear();

        // Récupération des données réelles
        List<String> historique = service.getListeTitresHistorique();
        List<Cours> suggestions = service.genererSuggestionsML();

        // 1. Mise à jour du Profil (Statique ou dynamique selon ton besoin)
        lblNomCandidat.setText("Sahar");
        int totalC = 10; // Objectif de cours
        int suivis = historique.size();
        lblStatsCours.setText("Cours suivis : " + suivis + " / " + totalC);

        double ratio = (totalC > 0) ? (double) suivis / totalC : 0;
        progressGlobal.setProgress(ratio);
        lblPercent.setText((int)(ratio * 100) + "%");

        // 2. Remplissage de la liste de l'historique dans la Sidebar
        for (String titre : historique) {
            Label hLabel = new Label("✓ " + titre);
            hLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 3 0;");
            listeHistorique.getChildren().add(hLabel);
        }

        // 3. Génération des Cartes de Suggestions (Sans bouton "Voir")
        if (suggestions.isEmpty()) {
            Label infoLabel = new Label("Aucune nouvelle suggestion pour le moment.");
            infoLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic;");
            containerCards.getChildren().add(infoLabel);
        } else {
            for (Cours c : suggestions) {
                containerCards.getChildren().add(creerCarteCours(c));
            }
        }
    }

    /**
     * Crée une carte visuelle pour un cours suggéré (Information uniquement)
     */
    private HBox creerCarteCours(Cours c) {
        HBox card = new HBox(25);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 18; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 5);");

        VBox info = new VBox(10);

        // Titre du cours
        Label title = new Label(c.getTitle());
        title.setStyle("-fx-font-size: 19; -fx-font-weight: bold; -fx-text-fill: #2D1B4E;");

        // Badge de niveau
        Label badge = new Label(c.getLevel() != null ? c.getLevel().toUpperCase() : "GÉNÉRAL");
        badge.setStyle("-fx-background-color: #FEF5E7; -fx-text-fill: #F39C12; -fx-padding: 5 12; " +
                "-fx-background-radius: 12; -fx-font-size: 10; -fx-font-weight: bold;");

        // Catégorie
        Label desc = new Label("Domaine : " + (c.getCategory() != null ? c.getCategory() : "Non classé"));
        desc.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 13;");

        info.getChildren().addAll(title, badge, desc);
        HBox.setHgrow(info, Priority.ALWAYS);

        // On ajoute uniquement les infos à la carte
        card.getChildren().add(info);

        return card;
    }

    /**
     * Retourne à la liste des cours via le Dashboard
     */
    @FXML
    private void handleRetour(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AffichierCoursStudent.fxml", "Mes Cours");
    }
}