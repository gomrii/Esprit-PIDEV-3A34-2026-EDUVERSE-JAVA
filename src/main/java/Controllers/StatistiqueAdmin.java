package Controllers;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.ServiceCours;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.Cursor;

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
                // L'animation nécessite que le node soit créé (après l'ajout)
                javafx.application.Platform.runLater(() -> animateNodeOnHover(data.getNode()));
            });

            // 2. Data BarChart
            levelBarChart.getData().clear();
            XYChart.Series<String, Number> lSeries = new XYChart.Series<>();
            lSeries.setName("Nombre de cours");
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

            // Animations d'entrée
            startEntranceAnimation(paneCat, 0);
            startEntranceAnimation(paneNiveau, 200);
            startEntranceAnimation(paneChap, 400);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startEntranceAnimation(Node node, int delay) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(800), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delay));

        TranslateTransition tt = new TranslateTransition(Duration.millis(800), node);
        tt.setFromY(20);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delay));

        ft.play();
        tt.play();
    }

    private void animateNodeOnHover(Node node) {
        if (node != null) {
            node.setOnMouseEntered(e -> {
                node.setScaleX(1.05);
                node.setScaleY(1.05);
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
        // ✅ NAVIGATION HARMONISÉE : Retour à la gestion des cours via le Dashboard
        MainDashboardController.getInstance().loadView("AffichierCoursAdmin.fxml", "Gestion des Cours");
    }
}