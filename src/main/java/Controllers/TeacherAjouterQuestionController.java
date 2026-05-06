package Controllers;

import Entities.Question;
import Entities.Quiz;
import Services.QuestionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class TeacherAjouterQuestionController {

    @FXML
    private TextArea questionTA;

    private final QuestionService questionService = new QuestionService();
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
    private Quiz selectedQuiz;

    public void setQuiz(Quiz quiz) {
        this.selectedQuiz = quiz;
    }

    @FXML
    public void ajouterQuestion() {
        if (selectedQuiz == null) {
            ControllerUtils.showError("Erreur : quiz non selectionne.");
            return;
        }

        if (!ControllerUtils.isTextValid(questionTA)) {
            ControllerUtils.showError("Le texte de la question doit etre renseigne et contenir au moins 6 caracteres.");
            return;
        }

        Question question = new Question(questionTA.getText().trim(), selectedQuiz.getIdQuiz());

        try {
            questionService.ajouterQuestion(question);
            ControllerUtils.showInfo("Question ajoutee avec succes.");
            goBackToQuestionList();
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
        goBackToQuestionList();
    }

    private void goBackToQuestionList() {
        try {
            FXMLLoader loader = ControllerUtils.createLoader("/teacher_question_list.fxml");
            Parent root = loader.load();
            TeacherQuestionListController controller = loader.getController();
            controller.setQuiz(selectedQuiz);

            Stage stage = (Stage) questionTA.getScene().getWindow();
            ControllerUtils.openInApplication(stage, root, "Gestion Questions");
        } catch (IOException | IllegalStateException e) {
            ControllerUtils.showError("Impossible de retourner a la liste des questions : " + e.getMessage());
        }
    }
}
