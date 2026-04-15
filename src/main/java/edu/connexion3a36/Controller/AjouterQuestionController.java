package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuestionService;
import edu.connexion3a36.services.QuizService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterQuestionController {

    @FXML
    private TextArea questionTA;

    @FXML
    private ChoiceBox<Quiz> quizChoice;

    private final QuestionService questionService = new QuestionService();
    private final QuizService quizService = new QuizService();
    private Quiz selectedQuiz;
    private String actorType = "admin";

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public void setSelectedQuiz(Quiz quiz) {
        selectedQuiz = quiz;
        if (quizChoice != null && quiz != null) {
            quizChoice.setValue(findQuizById(quiz.getIdQuiz()));
            quizChoice.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        quizChoice.getItems().clear();
        try {
            List<Quiz> quizzes = quizService.afficherQuiz();
            quizChoice.setItems(FXCollections.observableArrayList(quizzes));
            if (selectedQuiz != null) {
                quizChoice.setValue(findQuizById(selectedQuiz.getIdQuiz()));
                quizChoice.setDisable(true);
            }
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }

    @FXML
    public void ajouterQuestion() {
        if (!ControllerUtils.isTextValid(questionTA)) {
            ControllerUtils.showError("Le texte de la question doit être renseigné et contenir au moins 6 caractères.");
            return;
        }

        Quiz quiz = quizChoice.getValue();
        if (quiz == null) {
            ControllerUtils.showError("Un quiz doit être sélectionné.");
            return;
        }

        Question question = new Question(questionTA.getText().trim(), quiz.getIdQuiz());

        try {
            questionService.ajouterQuestion(question);
            ControllerUtils.showInfo("Question ajoutée avec succès.");
            goBackToQuestionList(quiz.getIdQuiz());
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'ajout de la question : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        Quiz quiz = quizChoice.getValue();
        goBackToQuestionList(quiz != null ? quiz.getIdQuiz() : (selectedQuiz != null ? selectedQuiz.getIdQuiz() : -1));
    }

    private Quiz findQuizById(int idQuiz) {
        for (Quiz quiz : quizChoice.getItems()) {
            if (quiz.getIdQuiz() == idQuiz) {
                return quiz;
            }
        }
        Quiz fallback = new Quiz();
        fallback.setIdQuiz(idQuiz);
        return fallback;
    }

    private void goBackToQuestionList(int targetQuizId) {
        try {
            if ("admin".equals(actorType)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_question_list.fxml"));
                Parent root = loader.load();
                AdminQuestionListController controller = loader.getController();
                if (targetQuizId >= 0) {
                    controller.setQuizId(targetQuizId);
                }
                switchScene(root);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/question_list.fxml"));
            Parent root = loader.load();
            switchScene(root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner : " + e.getMessage());
        }
    }

    private void switchScene(Parent root) {
        Stage stage = (Stage) questionTA.getScene().getWindow();
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
