package Controllers;

import Services.DashboardStatsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Locale;

public class StudentStatsController {
    private static final String STATUS_GREEN = "#20B486";
    private static final String STATUS_YELLOW = "#F4C542";
    private static final String BAR_BLUE = "#4F8DFD";

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
    private Label progressDonutValueLabel;
    @FXML
    private Label progressDonutCaptionLabel;
    @FXML
    private Label participationRateValueLabel;
    @FXML
    private Label successRateValueLabel;
    @FXML
    private Label averageRateValueLabel;
    @FXML
    private ProgressBar participationRateBar;
    @FXML
    private ProgressBar successRateBar;
    @FXML
    private ProgressBar averageRateBar;
    @FXML
    private PieChart progressPieChart;
    @FXML
    private BarChart<String, Number> scoresBarChart;
    @FXML
    private VBox progressEmptyState;
    @FXML
    private VBox scoresEmptyState;

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
            bindScoreChart(stats);
            bindSummary(stats);
        } catch (SQLException e) {
            ControllerUtils.showError("Impossible de charger les statistiques etudiant : " + e.getMessage());
        }
    }

    private void configureCharts() {
        progressPieChart.setClockwise(true);
        progressPieChart.setStartAngle(90);
        progressPieChart.setLabelsVisible(false);
        progressPieChart.setLegendVisible(false);

        scoresBarChart.setAnimated(false);
        scoresBarChart.setCategoryGap(30);
        scoresBarChart.setBarGap(10);
        if (scoresBarChart.getXAxis() != null) {
            scoresBarChart.getXAxis().setAnimated(false);
        }
        if (scoresBarChart.getYAxis() instanceof NumberAxis numberAxis) {
            numberAxis.setAnimated(false);
            numberAxis.setAutoRanging(false);
            numberAxis.setLowerBound(0);
            numberAxis.setUpperBound(100);
            numberAxis.setTickUnit(20);
            numberAxis.setForceZeroInRange(true);
            numberAxis.setMinorTickVisible(false);
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
        int passed = stats.getPassedQuizCount();
        int successful = stats.getSuccessfulQuizCount();
        int failed = Math.max(passed - successful, 0);
        boolean hasAttempts = passed > 0;

        progressEmptyState.setManaged(!hasAttempts);
        progressEmptyState.setVisible(!hasAttempts);
        progressPieChart.setManaged(hasAttempts);
        progressPieChart.setVisible(hasAttempts);

        progressDonutValueLabel.setText(String.valueOf(passed));
        progressDonutCaptionLabel.setText(hasAttempts ? "tentatives" : "aucune tentative");

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Reussis", successful),
                new PieChart.Data("A ameliorer", failed)
        );
        progressPieChart.setData(pieData);

        Platform.runLater(() -> {
            applyPieSliceColor(pieData, 0, STATUS_GREEN);
            applyPieSliceColor(pieData, 1, STATUS_YELLOW);
        });
    }

    private void bindScoreChart(DashboardStatsService.StudentStats stats) {
        boolean hasScores = stats.getPassedQuizCount() > 0;
        scoresEmptyState.setManaged(!hasScores);
        scoresEmptyState.setVisible(!hasScores);
        scoresBarChart.setManaged(hasScores);
        scoresBarChart.setVisible(hasScores);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Scores");
        series.getData().add(new XYChart.Data<>("Meilleur", stats.getBestScorePercentage()));
        series.getData().add(new XYChart.Data<>("Dernier", stats.getLastScorePercentage()));
        series.getData().add(new XYChart.Data<>("Moyenne", stats.getAverageScorePercentage()));
        scoresBarChart.getData().setAll(series);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + BAR_BLUE + ";");
                }
            }
        });
    }

    private void bindSummary(DashboardStatsService.StudentStats stats) {
        double participationRate = toRate(stats.getPassedQuizCount(), stats.getAvailableValidQuiz());
        double successRate = toRate(stats.getSuccessfulQuizCount(), stats.getPassedQuizCount());
        double averageRate = clamp(stats.getAverageScorePercentage());

        participationRateValueLabel.setText(formatRate(participationRate));
        successRateValueLabel.setText(formatRate(successRate));
        averageRateValueLabel.setText(formatRate(averageRate));

        participationRateBar.setProgress(participationRate / 100.0);
        successRateBar.setProgress(successRate / 100.0);
        averageRateBar.setProgress(averageRate / 100.0);
    }

    private void applyPieSliceColor(ObservableList<PieChart.Data> pieData, int index, String color) {
        if (index >= 0 && index < pieData.size() && pieData.get(index).getNode() != null) {
            pieData.get(index).getNode().setStyle("-fx-pie-color: " + color + ";");
        }
    }

    private double toRate(int value, int total) {
        if (total <= 0) {
            return 0;
        }
        return (value * 100.0) / total;
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }

    private String formatRate(double value) {
        return String.format(Locale.US, "%.0f %%", value);
    }
}
