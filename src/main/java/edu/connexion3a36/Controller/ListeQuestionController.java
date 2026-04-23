package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.services.QuestionService;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

public class ListeQuestionController {

    @FXML
    private VBox cardsContainer;
    @FXML
    private Label summaryLabel;
    @FXML
    private TextField searchTF;
    @FXML
    private ChoiceBox<String> sortChoice;

    private final QuestionService questionService = new QuestionService();
    private ObservableList<Question> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID decroissant",
                "Question A-Z",
                "Question Z-A"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        searchTF.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        chargerQuestions();
    }

    public void chargerQuestions() {
        try {
            masterData = FXCollections.observableArrayList(questionService.afficherQuestion());
            applyFiltersAndSort();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des questions : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        openQuestionForm(event, null, "/question_add.fxml");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchTF != null) {
            searchTF.clear();
        }
        chargerQuestions();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner a l'accueil: " + e.getMessage());
        }
    }

    private void handleEdit(Question question, ActionEvent event) {
        openQuestionForm(event, question, "/question_edit.fxml");
    }

    private void handleDelete(Question question) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la question selectionnee ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) {
            return;
        }
        try {
            questionService.supprimerQuestion(question.getIdQuestion());
            ControllerUtils.showInfo("Question supprimee.");
            chargerQuestions();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void openQuestionForm(ActionEvent event, Question question, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (question != null && controller != null) {
                try {
                    controller.getClass().getMethod("setQuestion", Question.class).invoke(controller, question);
                } catch (Exception ignored) {
                }
            }
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la page: " + e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Question> filtered = FXCollections.observableArrayList();
        for (Question item : masterData) {
            boolean matches = q.isEmpty()
                    || String.valueOf(item.getIdQuestion()).contains(q)
                    || safeValue(item.getQuestion()).toLowerCase().contains(q)
                    || String.valueOf(item.getIdQuiz()).contains(q);
            if (matches) {
                filtered.add(item);
            }
        }

        Comparator<Question> comparator = Comparator.comparingInt(Question::getIdQuestion);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID decroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Question::getIdQuestion).reversed();
        } else if ("Question A-Z".equals(sort)) {
            comparator = Comparator.comparing(question -> safeValue(question.getQuestion()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Question Z-A".equals(sort)) {
            comparator = Comparator.comparing((Question question) -> safeValue(question.getQuestion()), String.CASE_INSENSITIVE_ORDER).reversed();
        }
        FXCollections.sort(filtered, comparator);
        renderQuestionCards(filtered);
    }

    private void renderQuestionCards(ObservableList<Question> questions) {
        cardsContainer.getChildren().clear();
        summaryLabel.setText(questions.size() + (questions.size() > 1 ? " questions affichees" : " question affichee"));

        if (questions.isEmpty()) {
            cardsContainer.getChildren().add(createEmptyState("Aucune question trouvee", "Les resultats filtres apparaitront ici."));
            return;
        }

        for (Question question : questions) {
            cardsContainer.getChildren().add(createQuestionCard(question));
        }
    }

    private VBox createQuestionCard(Question question) {
        VBox card = new VBox(16);
        card.getStyleClass().add("dashboard-card");
        card.setPadding(new Insets(18));

        Label overline = new Label("Question #" + question.getIdQuestion());
        overline.getStyleClass().add("card-overline");

        Label title = new Label(safeValue(question.getQuestion()));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        HBox header = new HBox(12, new VBox(6, overline, title), createStatusBadge("Quiz #" + question.getIdQuiz(), "status-neutral"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);
        header.setAlignment(Pos.TOP_LEFT);

        Button edit = new Button("Modifier");
        edit.getStyleClass().add("btn-modifier");
        edit.setOnAction(event -> handleEdit(question, event));

        Button delete = new Button("Supprimer");
        delete.getStyleClass().add("btn-supprimer");
        delete.setOnAction(event -> handleDelete(question));

        HBox actions = new HBox(10, edit, delete);
        actions.getStyleClass().add("card-actions");

        card.getChildren().addAll(header, actions);
        return card;
    }

    private Label createStatusBadge(String text, String styleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("status-badge", styleClass);
        return badge;
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

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private void switchScene(ActionEvent event, Parent root) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
