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

public class TeacherStatsController {
    @FXML
    private Label myQuizValueLabel;
    @FXML
    private Label validatedQuizValueLabel;
    @FXML
    private Label pendingQuizValueLabel;
    @FXML
    private Label rejectedQuizValueLabel;
    @FXML
    private Label totalQuestionsValueLabel;
    @FXML
    private Label totalResponsesValueLabel;
    @FXML
    private PieChart statusPieChart;
    @FXML
    private BarChart<String, Number> totalsBarChart;

    private final DashboardStatsService dashboardStatsService = new DashboardStatsService();

    @FXML
    public void initialize() {
        configureCharts();
        loadStatistics();
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        ControllerUtils.navigateTo((Node) event.getSource(), "/teacher_quiz_list.fxml");
    }

    private void loadStatistics() {
        try {
            DashboardStatsService.TeacherStats stats = dashboardStatsService.fetchTeacherStats();
            bindCards(stats);
            bindStatusChart(stats);
            bindVolumeChart(stats);
        } catch (SQLException e) {
            ControllerUtils.showError("Impossible de charger les statistiques enseignant : " + e.getMessage());
        }
    }

    private void configureCharts() {
        statusPieChart.setClockwise(true);
        statusPieChart.setStartAngle(90);
        statusPieChart.setLabelsVisible(true);
        statusPieChart.setLegendVisible(true);

        totalsBarChart.setAnimated(false);
        totalsBarChart.setCategoryGap(22);
        totalsBarChart.setBarGap(8);
        if (totalsBarChart.getXAxis() != null) {
            totalsBarChart.getXAxis().setAnimated(false);
        }
        if (totalsBarChart.getYAxis() != null) {
            totalsBarChart.getYAxis().setAnimated(false);
            totalsBarChart.getYAxis().setAutoRanging(true);
        }
    }

    private void bindCards(DashboardStatsService.TeacherStats stats) {
        myQuizValueLabel.setText(String.valueOf(stats.getTotalQuiz()));
        validatedQuizValueLabel.setText(String.valueOf(stats.getValidatedQuiz()));
        pendingQuizValueLabel.setText(String.valueOf(stats.getPendingQuiz()));
        rejectedQuizValueLabel.setText(String.valueOf(stats.getRejectedQuiz()));
        totalQuestionsValueLabel.setText(String.valueOf(stats.getTotalQuestions()));
        totalResponsesValueLabel.setText(String.valueOf(stats.getTotalResponses()));
    }

    private void bindStatusChart(DashboardStatsService.TeacherStats stats) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Validés", stats.getValidatedQuiz()),
                new PieChart.Data("En attente", stats.getPendingQuiz()),
                new PieChart.Data("Rejetés", stats.getRejectedQuiz())
        );
        statusPieChart.setData(pieData);
    }

    private void bindVolumeChart(DashboardStatsService.TeacherStats stats) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Contenu");
        series.getData().add(new XYChart.Data<>("Mes quiz", stats.getTotalQuiz()));
        series.getData().add(new XYChart.Data<>("Questions", stats.getTotalQuestions()));
        series.getData().add(new XYChart.Data<>("Réponses", stats.getTotalResponses()));
        totalsBarChart.getData().setAll(series);
    }
}
