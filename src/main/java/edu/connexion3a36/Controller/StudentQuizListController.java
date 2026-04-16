package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuizService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;

public class StudentQuizListController {

    @FXML
    private VBox cardsContainer;
    @FXML
    private TextField searchTF;
    @FXML
    private ChoiceBox<String> sortChoice;
    @FXML
    private Label summaryLabel;
    private final QuizService quizService = new QuizService();
    private ObservableList<Quiz> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID decroissant",
                "Titre A-Z",
                "Titre Z-A",
                "Niveau",
                "Duree croissante",
                "Duree decroissante"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        chargerQuizValides();
    }

    public void chargerQuizValides() {
        try {
            masterData = FXCollections.observableArrayList(quizService.afficherQuizValides());
            applyFiltersAndSort();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des quiz valides : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchTF != null) {
            searchTF.clear();
        }
        chargerQuizValides();
    }

    @FXML
    private void handleOpenStatistics(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/student_stats.fxml"));
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la page statistiques : " + e.getMessage());
        }
    }

    private void handlePassQuiz(Quiz quiz, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/student_quiz_pass.fxml"));
            Parent root = loader.load();
            StudentQuizPassController controller = loader.getController();
            controller.setQuiz(quiz);
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le quiz : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        ControllerUtils.navigateTo((Node) event.getSource(), "/home.fxml");
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Quiz> filtered = FXCollections.observableArrayList();
        for (Quiz quiz : masterData) {
            if (q.isEmpty()
                    || String.valueOf(quiz.getIdQuiz()).contains(q)
                    || safeValue(quiz.getTitre()).toLowerCase().contains(q)
                    || safeValue(quiz.getStatut()).toLowerCase().contains(q)
                    || safeValue(quiz.getLevel()).toLowerCase().contains(q)
                    || String.valueOf(quiz.getDuree()).contains(q)) {
                filtered.add(quiz);
            }
        }

        Comparator<Quiz> comparator = Comparator.comparingInt(Quiz::getIdQuiz);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID decroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Quiz::getIdQuiz).reversed();
        } else if ("Titre A-Z".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getTitre()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Titre Z-A".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getTitre()), String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Niveau".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getLevel()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(Quiz::getIdQuiz);
        } else if ("Duree croissante".equals(sort)) {
            comparator = Comparator.comparingInt(Quiz::getDuree).thenComparingInt(Quiz::getIdQuiz);
        } else if ("Duree decroissante".equals(sort)) {
            comparator = Comparator.comparingInt(Quiz::getDuree).reversed().thenComparingInt(Quiz::getIdQuiz);
        }

        FXCollections.sort(filtered, comparator);
        renderQuizCards(filtered);
    }

    private void renderQuizCards(ObservableList<Quiz> quizzes) {
        cardsContainer.getChildren().clear();
        summaryLabel.setText(quizzes.size() + (quizzes.size() > 1 ? " quiz affiches" : " quiz affiche"));

        if (quizzes.isEmpty()) {
            cardsContainer.getChildren().add(createEmptyState(
                    "Aucun quiz valide",
                    "Les quiz valides apparaitront ici des qu ils seront disponibles."
            ));
            return;
        }

        for (Quiz quiz : quizzes) {
            cardsContainer.getChildren().add(createQuizCard(quiz));
        }
    }

    private VBox createQuizCard(Quiz quiz) {
        VBox card = new VBox(16);
        card.getStyleClass().add("dashboard-card");
        card.setPadding(new Insets(18));

        Label overline = new Label("Quiz #" + quiz.getIdQuiz());
        overline.getStyleClass().add("card-overline");

        Label title = new Label(safeValue(quiz.getTitre()));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        HBox topRow = new HBox(12, new VBox(6, overline, title), createStatusBadge(safeValue(quiz.getStatut())));
        HBox.setHgrow(topRow.getChildren().get(0), Priority.ALWAYS);

        HBox metaRow = new HBox(10,
                createMetaChip("Niveau", safeValue(quiz.getLevel())),
                createMetaChip("Duree", quiz.getDuree() + " min")
        );
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Button passButton = new Button("Passer Quiz");
        passButton.getStyleClass().add("btn-success");
        passButton.setOnAction(event -> handlePassQuiz(quiz, event));

        HBox actions = new HBox(10, passButton);
        actions.getStyleClass().add("card-actions");

        card.getChildren().addAll(topRow, metaRow, actions);
        return card;
    }

    private Label createStatusBadge(String status) {
        Label badge = new Label(status.isBlank() ? "Sans statut" : status);
        badge.getStyleClass().addAll("status-badge", statusStyle(status));
        return badge;
    }

    private VBox createMetaChip(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("meta-label");
        Label value = new Label(valueText == null || valueText.isBlank() ? "-" : valueText);
        value.getStyleClass().add("meta-value");

        VBox chip = new VBox(4, label, value);
        chip.getStyleClass().add("meta-chip");
        return chip;
    }

    private VBox createEmptyState(String titleText, String bodyText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("empty-state-title");
        Label body = new Label(bodyText);
        body.getStyleClass().add("empty-state-text");
        body.setWrapText(true);

        VBox box = new VBox(8, title, body);
        box.getStyleClass().add("empty-state");
        return box;
    }

    private String statusStyle(String status) {
        String normalized = safeValue(status).toLowerCase();
        if (normalized.contains("valid")) {
            return "status-valid";
        }
        if (normalized.contains("rejet")) {
            return "status-rejected";
        }
        if (normalized.contains("attente")) {
            return "status-pending";
        }
        return "status-neutral";
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private void switchScene(ActionEvent event, Parent root) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene == null) {
            Scene newScene = new Scene(root, 1100, 700);
            newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(newScene);
        } else {
            scene.setRoot(root);
        }
    }
}
