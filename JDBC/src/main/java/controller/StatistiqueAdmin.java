package controller;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.ServiceCours;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Cursor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatistiqueAdmin {

    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> levelBarChart;
    @FXML private AreaChart<String, Number> contentAreaChart;
    @FXML private VBox paneCat, paneNiveau, paneChap;

    private final ServiceCours sc = new ServiceCours();
    private final ServiceChapitre sch = new ServiceChapitre();

    @FXML
    public void initialize() {
        loadStats();
    }

    @FXML
    private void handleRefresh() {
        loadStats();
    }

    private void loadStats() {
        try {
            List<Cours> cours = sc.display();
            List<Chapitre> chapitres = sch.display();

            // 1. Data PieChart
            categoryPieChart.getData().clear();
            Map<String, Long> catMap = cours.stream()
                    .collect(Collectors.groupingBy(Cours::getCategory, Collectors.counting()));
            catMap.forEach((k, v) -> {
                PieChart.Data data = new PieChart.Data(k, v);
                categoryPieChart.getData().add(data);
                animateNodeOnHover(data.getNode());
            });

            // 2. Data BarChart
            levelBarChart.getData().clear();
            XYChart.Series<String, Number> lSeries = new XYChart.Series<>();
            lSeries.setName("Cours");
            Map<String, Long> lMap = cours.stream()
                    .collect(Collectors.groupingBy(Cours::getLevel, Collectors.counting()));
            lMap.forEach((k, v) -> lSeries.getData().add(new XYChart.Data<>(k, v)));
            levelBarChart.getData().add(lSeries);

            // 3. Data AreaChart
            contentAreaChart.getData().clear();
            XYChart.Series<String, Number> cSeries = new XYChart.Series<>();
            cSeries.setName("Nombre de chapitres");
            for (Cours c : cours) {
                long count = chapitres.stream().filter(ch -> ch.getCours_id() == c.getId()).count();
                if (count > 0) cSeries.getData().add(new XYChart.Data<>(c.getTitle(), count));
            }
            contentAreaChart.getData().add(cSeries);

            // Appliquer l'animation d'entrée aux conteneurs
            startEntranceAnimation(paneCat, 0);
            startEntranceAnimation(paneNiveau, 200);
            startEntranceAnimation(paneChap, 400);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startEntranceAnimation(Node node, int delay) {
        node.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.millis(1000), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delay));

        TranslateTransition tt = new TranslateTransition(Duration.millis(1000), node);
        tt.setFromY(30);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delay));

        ft.play();
        tt.play();
    }

    private void animateNodeOnHover(Node node) {
        if (node != null) {
            node.setOnMouseEntered(e -> {
                node.setScaleX(1.08);
                node.setScaleY(1.08);
                node.setCursor(Cursor.HAND);
            });
            node.setOnMouseExited(e -> {
                node.setScaleX(1.0);
                node.setScaleY(1.0);
            });
        }
    }
    @FXML
    void gotocours(ActionEvent event) {
        try {
            // Chargement du fichier FXML de l'administration des cours
            // Vérifie bien que le nom est exactement "AffichierCoursAdmin.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierCoursAdmin.fxml"));
            Parent root = loader.load();

            // Récupération du Stage (la fenêtre) actuel
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Création et application de la nouvelle scène
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Optionnel : centrer la fenêtre si la taille change
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur de navigation vers Admin Cours : " + e.getMessage());
            e.printStackTrace();

            // Alerte visuelle en cas d'erreur (fichier manquant par exemple)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger la page AffichierCoursAdmin.");
            alert.show();
        }
    }
}