package Controllers;

import Entities.Question;
import Entities.Quiz;
import Services.QuestionService;
import Services.QuizService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
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
            ControllerUtils.showError("Le texte de la question doit etre renseigne et contenir au moins 6 caracteres.");
            return;
        }

        Quiz quiz = quizChoice.getValue();
        if (quiz == null) {
            ControllerUtils.showError("Un quiz doit etre selectionne.");
            return;
        }

        Question question = new Question(questionTA.getText().trim(), quiz.getIdQuiz());

        try {
            questionService.ajouterQuestion(question);
            ControllerUtils.showInfo("Question ajoutee avec succes.");
            goBackToQuestionList(quiz.getIdQuiz());
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'ajout de la question : " + e.getMessage());
        }
    }

    @FXML
    private void handleQuestionSpeech(javafx.event.ActionEvent event) {
        ControllerUtils.startSpeechToText((Button) event.getSource(), questionTA);
    }

    @FXML
    private void handleQuestionGrammar(javafx.event.ActionEvent event) {
        saplingUiSupport.handleGrammarCheck((Button) event.getSource(), questionTA);
    }

    @FXML
    private void handleQuestionTranslateEn(javafx.event.ActionEvent event) {
        translationUiSupport.translateToEnglish((Button) event.getSource(), questionTA);
    }

    @FXML
    private void handleQuestionTranslateFr(javafx.event.ActionEvent event) {
        translationUiSupport.translateToFrench((Button) event.getSource(), questionTA);
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
                FXMLLoader loader = ControllerUtils.createLoader("/admin_question_list.fxml");
                Parent root = loader.load();
                AdminQuestionListController controller = loader.getController();
                if (targetQuizId >= 0) {
                    controller.setQuizId(targetQuizId);
                }
                switchScene(root);
                return;
            }

            FXMLLoader loader = ControllerUtils.createLoader("/teacher_question_list.fxml");
            Parent root = loader.load();
            TeacherQuestionListController controller = loader.getController();
            if (targetQuizId >= 0) {
                controller.setQuizId(targetQuizId);
            }
            switchScene(root);
        } catch (IOException | IllegalStateException e) {
            ControllerUtils.showError("Impossible de retourner a la liste des questions : " + e.getMessage());
        }
    }

    private void switchScene(Parent root) {
        Stage stage = (Stage) questionTA.getScene().getWindow();
        ControllerUtils.openInApplication(stage, root, "Gestion Questions");
    }
}
