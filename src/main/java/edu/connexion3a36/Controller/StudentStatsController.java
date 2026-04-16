package edu.connexion3a36.Controller;

import edu.connexion3a36.services.DashboardStatsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class StudentStatsController {
    @FXML
    private Label availableQuizValueLabel;
    @FXML
    private Label passedQuizValueLabel;
    @FXML
    private Label bestScoreValueLabel;
    @FXML
    private Label lastScoreValueLabel;
    @FXML
    private Label averageScoreValueLabel;
    @FXML
    private Label successfulQuizValueLabel;
    @FXML
    private PieChart progressPieChart;
    @FXML
    private BarChart<String, Number> scoresBarChart;

    private final DashboardStatsService dashboardStatsService = new DashboardStatsService();

    @FXML
    public void initialize() {
        configureCharts();
        loadStatistics();
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        ControllerUtils.navigateTo((Node) event.getSource(), "/student_quiz_list.fxml");
    }

    private void loadStatistics() {
        try {
            DashboardStatsService.StudentStats stats = dashboardStatsService.fetchStudentStats();
            bindCards(stats);
            bindProgressChart(stats);
            bindVolumeChart(stats);
        } catch (SQLException e) {
            ControllerUtils.showError("Impossible de charger les statistiques étudiant : " + e.getMessage());
        }
    }

    private void configureCharts() {
        progressPieChart.setClockwise(true);
        progressPieChart.setStartAngle(90);
        progressPieChart.setLabelsVisible(true);
        progressPieChart.setLegendVisible(true);

        scoresBarChart.setAnimated(false);
        scoresBarChart.setCategoryGap(22);
        scoresBarChart.setBarGap(8);
        if (scoresBarChart.getXAxis() != null) {
            scoresBarChart.getXAxis().setAnimated(false);
        }
        if (scoresBarChart.getYAxis() != null) {
            scoresBarChart.getYAxis().setAnimated(false);
            scoresBarChart.getYAxis().setAutoRanging(true);
        }
    }

    private void bindCards(DashboardStatsService.StudentStats stats) {
        availableQuizValueLabel.setText(String.valueOf(stats.getAvailableValidQuiz()));
        passedQuizValueLabel.setText(String.valueOf(stats.getPassedQuizCount()));
        bestScoreValueLabel.setText(stats.getBestScore());
        lastScoreValueLabel.setText(stats.getLastScore());
        averageScoreValueLabel.setText(stats.getAverageScore());
        successfulQuizValueLabel.setText(String.valueOf(stats.getSuccessfulQuizCount()));
    }

    private void bindProgressChart(DashboardStatsService.StudentStats stats) {
        int failed = Math.max(stats.getPassedQuizCount() - stats.getSuccessfulQuizCount(), 0);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Réussis", stats.getSuccessfulQuizCount()),
                new PieChart.Data("À améliorer", failed)
        );
        progressPieChart.setData(pieData);
    }

    private void bindVolumeChart(DashboardStatsService.StudentStats stats) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Repères");
        series.getData().add(new XYChart.Data<>("Disponibles", stats.getAvailableValidQuiz()));
        series.getData().add(new XYChart.Data<>("Passés", stats.getPassedQuizCount()));
        series.getData().add(new XYChart.Data<>("Réussis", stats.getSuccessfulQuizCount()));
        addScoreValue(series, "Meilleur", stats.getBestScore());
        addScoreValue(series, "Dernier", stats.getLastScore());
        scoresBarChart.getData().setAll(series);
    }

    private void addScoreValue(XYChart.Series<String, Number> series, String label, String formattedScore) {
        String[] parts = formattedScore.split("/");
        if (parts.length == 0) {
            series.getData().add(new XYChart.Data<>(label, 0));
            return;
        }
        try {
            series.getData().add(new XYChart.Data<>(label, Double.parseDouble(parts[0].trim().replace(',', '.'))));
        } catch (NumberFormatException e) {
            series.getData().add(new XYChart.Data<>(label, 0));
        }
    }
}
