package Controllers;

import Entities.Question;
import Services.QuestionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class TeacherModifierQuestionController {

    @FXML
    private TextArea questionTA;

    private final QuestionService questionService = new QuestionService();
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
    private Question selectedQuestion;

    public void setQuestion(Question question) {
        this.selectedQuestion = question;
        if (question != null && questionTA != null) {
            questionTA.setText(question.getQuestion());
        }
    }

    @FXML
    public void modifierQuestion() {
        if (selectedQuestion == null) {
            ControllerUtils.showWarning("Selectionnez une question a modifier.");
            return;
        }

        if (!ControllerUtils.isTextValid(questionTA)) {
            ControllerUtils.showError("Le texte de la question doit etre renseigne et contenir au moins 6 caracteres.");
            return;
        }

        selectedQuestion.setQuestion(questionTA.getText().trim());

        try {
            questionService.modifierQuestion(selectedQuestion);
            ControllerUtils.showInfo("Question modifiee avec succes.");
            goBackToQuestionList();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la modification de la question : " + e.getMessage());
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
            if (selectedQuestion != null) {
                controller.setQuizId(selectedQuestion.getIdQuiz());
            }

            Stage stage = (Stage) questionTA.getScene().getWindow();
            ControllerUtils.openInApplication(stage, root, "Gestion Questions");
        } catch (IOException | IllegalStateException e) {
            ControllerUtils.showError("Impossible de retourner a la liste des questions : " + e.getMessage());
        }
    }
}
