package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.services.QuestionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.sql.SQLException;

public class ListeQuestionController {

    @FXML
    private TableView<Question> questionsTable;

    @FXML
    private TableColumn<Question, Integer> colId;

    @FXML
    private TableColumn<Question, String> colQuestion;

    @FXML
    private TableColumn<Question, Integer> colIdQuiz;

    private final QuestionService questionService = new QuestionService();
    private ObservableList<Question> masterData = FXCollections.observableArrayList();

    @FXML
    private javafx.scene.control.TextField searchTF;

    @FXML
    private javafx.scene.control.ChoiceBox<String> sortChoice;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idQuestion"));
        colQuestion.setCellValueFactory(new PropertyValueFactory<>("question"));
        colIdQuiz.setCellValueFactory(new PropertyValueFactory<>("idQuiz"));
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID décroissant",
                "Question A-Z",
                "Question Z-A"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
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
    private void handleSearch(javafx.event.ActionEvent event) {
        applyFiltersAndSort();
    }

    public Question getSelectedQuestion() {
        return questionsTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleAdd(ActionEvent event) { openFXML(event, "/question_add.fxml"); }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (getSelectedQuestion() == null) { ControllerUtils.showWarning("Sélectionnez une question à modifier."); return; }
        openFXML(event, "/question_edit.fxml");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        // kept for compatibility with FXML using handleDelete
        supprimerQuestion(event);
    }

    @FXML
    private void supprimerQuestion(ActionEvent event) {
        if (getSelectedQuestion() == null) {
            ControllerUtils.showWarning("Sélectionnez une question à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la question sélectionnée ?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == javafx.scene.control.ButtonType.YES) {
            try {
                questionService.supprimerQuestion(getSelectedQuestion().getIdQuestion());
                ControllerUtils.showInfo("Question supprimée.");
                chargerQuestions();
            } catch (SQLException e) {
                ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) { chargerQuestions(); }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
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
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }

    private void openFXML(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            try {
                if (controller != null) {
                    java.lang.reflect.Method m = null;
                    try { m = controller.getClass().getMethod("setQuestion", Question.class); } catch (NoSuchMethodException ignored) {}
                    if (m != null) m.invoke(controller, getSelectedQuestion());
                }
            } catch (Exception ignored) {}
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
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
                    || (item.getQuestion() != null && item.getQuestion().toLowerCase().contains(q))
                    || String.valueOf(item.getIdQuiz()).contains(q);
            if (matches) {
                filtered.add(item);
            }
        }

        Comparator<Question> comparator = Comparator.comparingInt(Question::getIdQuestion);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID décroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Question::getIdQuestion).reversed();
        } else if ("Question A-Z".equals(sort)) {
            comparator = Comparator.comparing(question -> safeValue(question.getQuestion()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Question Z-A".equals(sort)) {
            comparator = Comparator.comparing((Question question) -> safeValue(question.getQuestion()), String.CASE_INSENSITIVE_ORDER).reversed();
        }
        FXCollections.sort(filtered, comparator);
        questionsTable.setItems(filtered);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
