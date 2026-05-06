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

import java.sql.SQLException;
import java.util.Locale;

public class AdminStatsController {
    private static final String STATUS_GREEN = "#20B486";
    private static final String STATUS_YELLOW = "#F4C542";
    private static final String STATUS_RED = "#E84A5F";
    private static final String BAR_BLUE = "#4F8DFD";

    @FXML
    private Label totalQuizValueLabel;
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
    private Label adminCreatedValueLabel;
    @FXML
    private Label teacherCreatedValueLabel;
    @FXML
    private Label statusDonutValueLabel;
    @FXML
    private Label statusDonutCaptionLabel;
    @FXML
    private Label totalContentValueLabel;
    @FXML
    private Label validationRateValueLabel;
    @FXML
    private Label pendingRateValueLabel;
    @FXML
    private Label rejectedRateValueLabel;
    @FXML
    private ProgressBar validationRateBar;
    @FXML
    private ProgressBar pendingRateBar;
    @FXML
    private ProgressBar rejectedRateBar;
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
        ControllerUtils.navigateTo((Node) event.getSource(), "/admin_quiz_list.fxml");
    }

    private void loadStatistics() {
        try {
            DashboardStatsService.AdminStats stats = dashboardStatsService.fetchAdminStats();
            bindCards(stats);
            bindStatusChart(stats);
            bindVolumeChart(stats);
            bindSummary(stats);
        } catch (SQLException e) {
            ControllerUtils.showError("Impossible de charger les statistiques admin : " + e.getMessage());
        }
    }

    private void configureCharts() {
        statusPieChart.setClockwise(true);
        statusPieChart.setStartAngle(90);
        statusPieChart.setLabelsVisible(false);
        statusPieChart.setLegendVisible(false);

        totalsBarChart.setAnimated(false);
        totalsBarChart.setCategoryGap(30);
        totalsBarChart.setBarGap(10);
        if (totalsBarChart.getXAxis() != null) {
            totalsBarChart.getXAxis().setAnimated(false);
        }
        if (totalsBarChart.getYAxis() instanceof NumberAxis numberAxis) {
            numberAxis.setAnimated(false);
            numberAxis.setAutoRanging(true);
            numberAxis.setMinorTickVisible(false);
            numberAxis.setTickUnit(5);
        }
    }

    private void bindCards(DashboardStatsService.AdminStats stats) {
        totalQuizValueLabel.setText(String.valueOf(stats.getTotalQuiz()));
        validatedQuizValueLabel.setText(String.valueOf(stats.getValidatedQuiz()));
        pendingQuizValueLabel.setText(String.valueOf(stats.getPendingQuiz()));
        rejectedQuizValueLabel.setText(String.valueOf(stats.getRejectedQuiz()));
        totalQuestionsValueLabel.setText(String.valueOf(stats.getTotalQuestions()));
        totalResponsesValueLabel.setText(String.valueOf(stats.getTotalResponses()));
        adminCreatedValueLabel.setText(String.valueOf(stats.getAdminCreatedQuiz()));
        teacherCreatedValueLabel.setText(String.valueOf(stats.getTeacherCreatedQuiz()));
    }

    private void bindStatusChart(DashboardStatsService.AdminStats stats) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Valides", stats.getValidatedQuiz()),
                new PieChart.Data("En attente", stats.getPendingQuiz()),
                new PieChart.Data("Rejetes", stats.getRejectedQuiz())
        );
        statusPieChart.setData(pieData);

        int totalQuiz = Math.max(stats.getTotalQuiz(), 0);
        statusDonutValueLabel.setText(String.valueOf(totalQuiz));
        statusDonutCaptionLabel.setText(totalQuiz <= 1 ? "quiz suivi" : "quiz suivis");

        Platform.runLater(() -> {
            applyPieSliceColor(pieData, 0, STATUS_GREEN);
            applyPieSliceColor(pieData, 1, STATUS_YELLOW);
            applyPieSliceColor(pieData, 2, STATUS_RED);
        });
    }

    private void bindVolumeChart(DashboardStatsService.AdminStats stats) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Volumes");
        series.getData().add(new XYChart.Data<>("Quiz", stats.getTotalQuiz()));
        series.getData().add(new XYChart.Data<>("Questions", stats.getTotalQuestions()));
        series.getData().add(new XYChart.Data<>("Reponses", stats.getTotalResponses()));
        series.getData().add(new XYChart.Data<>("Admin", stats.getAdminCreatedQuiz()));
        series.getData().add(new XYChart.Data<>("Enseignant", stats.getTeacherCreatedQuiz()));
        totalsBarChart.getData().setAll(series);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + BAR_BLUE + ";");
                }
            }
        });
    }

    private void bindSummary(DashboardStatsService.AdminStats stats) {
        int totalQuiz = stats.getTotalQuiz();
        int totalContent = stats.getTotalQuiz() + stats.getTotalQuestions() + stats.getTotalResponses();

        double validationRate = toRate(stats.getValidatedQuiz(), totalQuiz);
        double pendingRate = toRate(stats.getPendingQuiz(), totalQuiz);
        double rejectedRate = toRate(stats.getRejectedQuiz(), totalQuiz);

        totalContentValueLabel.setText(String.valueOf(totalContent));
        validationRateValueLabel.setText(formatRate(validationRate));
        pendingRateValueLabel.setText(formatRate(pendingRate));
        rejectedRateValueLabel.setText(formatRate(rejectedRate));

        validationRateBar.setProgress(validationRate / 100.0);
        pendingRateBar.setProgress(pendingRate / 100.0);
        rejectedRateBar.setProgress(rejectedRate / 100.0);
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

    private String formatRate(double value) {
        return String.format(Locale.US, "%.0f %%", value);
    }
}
