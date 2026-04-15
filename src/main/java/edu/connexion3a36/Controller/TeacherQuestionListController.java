package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuestionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.sql.SQLException;

public class TeacherQuestionListController {

    @FXML
    private TableView<Question> questionsTable;

    @FXML
    private TableColumn<Question, Integer> colId;

    @FXML
    private TableColumn<Question, String> colQuestion;

    @FXML
    private Label quizTitleLabel;

    private final QuestionService questionService = new QuestionService();
    private ObservableList<Question> masterData = FXCollections.observableArrayList();
    private Quiz selectedQuiz;
    private int quizId = -1;

    @FXML
    private TextField searchTF;

    @FXML
    private ChoiceBox<String> sortChoice;

    public void setQuiz(Quiz quiz) {
        this.selectedQuiz = quiz;
        this.quizId = quiz.getIdQuiz();
        if (quiz != null) {
            quizTitleLabel.setText("Questions du quiz: " + quiz.getTitre());
            chargerQuestions();
        }
    }

    public void setQuizId(int idQuiz) {
        this.quizId = idQuiz;
        quizTitleLabel.setText("Questions du quiz (ID: " + idQuiz + ")");
        chargerQuestions();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idQuestion"));
        colQuestion.setCellValueFactory(new PropertyValueFactory<>("question"));
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID décroissant",
                "Question A-Z",
                "Question Z-A"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    public void chargerQuestions() {
        if (quizId < 0) return;
        try {
            masterData = FXCollections.observableArrayList(questionService.afficherQuestionsByIdQuiz(quizId));
            applyFiltersAndSort();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des questions : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    public Question getSelectedQuestion() {
        return questionsTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (selectedQuiz == null && quizId < 0) {
            ControllerUtils.showError("Erreur: Quiz non disponible.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_question_add.fxml"));
            Parent root = loader.load();
            TeacherAjouterQuestionController controller = loader.getController();
            if (selectedQuiz != null) {
                controller.setQuiz(selectedQuiz);
            } else {
                // Create a temporary quiz object with just the ID
                Quiz tempQuiz = new Quiz();
                tempQuiz.setIdQuiz(quizId);
                controller.setQuiz(tempQuiz);
            }
            
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
            ControllerUtils.showError("Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (getSelectedQuestion() == null) {
            ControllerUtils.showWarning("Sélectionnez une question à modifier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_question_edit.fxml"));
            Parent root = loader.load();
            TeacherModifierQuestionController controller = loader.getController();
            controller.setQuestion(getSelectedQuestion());
            
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
            ControllerUtils.showError("Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (getSelectedQuestion() == null) {
            ControllerUtils.showWarning("Sélectionnez une question à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la question sélectionnée ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
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
    private void handleManageAnswers(ActionEvent event) {
        if (getSelectedQuestion() == null) {
            ControllerUtils.showWarning("Sélectionnez une question pour gérer ses réponses.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_answer_list.fxml"));
            Parent root = loader.load();
            TeacherAnswerListController controller = loader.getController();
            controller.setQuestion(getSelectedQuestion());
            
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
            ControllerUtils.showError("Impossible d'ouvrir la gestion des réponses: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        chargerQuestions();
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Question> filtered = FXCollections.observableArrayList();
        for (Question item : masterData) {
            if (q.isEmpty()
                    || String.valueOf(item.getIdQuestion()).contains(q)
                    || (item.getQuestion() != null && item.getQuestion().toLowerCase().contains(q))) {
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

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_quiz_list.fxml"));
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
            ControllerUtils.showError("Impossible de retourner aux quiz: " + e.getMessage());
        }
    }
}
