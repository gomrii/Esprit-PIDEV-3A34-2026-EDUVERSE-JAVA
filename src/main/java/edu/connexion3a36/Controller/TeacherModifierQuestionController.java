package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.services.QuestionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class TeacherModifierQuestionController {

    @FXML
    private TextArea questionTA;

    private final QuestionService questionService = new QuestionService();
    private Question selectedQuestion;

    public void setQuestion(Question question) {
        this.selectedQuestion = question;
        if (question != null) {
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
            ControllerUtils.showError("Le texte de la question doit être renseigné.");
            return;
        }

        selectedQuestion.setQuestion(questionTA.getText().trim());

        try {
            questionService.modifierQuestion(selectedQuestion);
            ControllerUtils.showInfo("Question modifiée avec succès.");
            // Navigate back to question list with quiz
            goBackToQuestionList();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la modification de la question : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        goBackToQuestionList();
    }

    private void goBackToQuestionList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_question_list.fxml"));
            Parent root = loader.load();
            TeacherQuestionListController controller = loader.getController();
            // Need to pass the idQuiz from the selectedQuestion
            if (selectedQuestion != null) {
                controller.setQuizId(selectedQuestion.getIdQuiz());
            }

            Stage stage = (Stage) questionTA.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner : " + e.getMessage());
        }
    }
}