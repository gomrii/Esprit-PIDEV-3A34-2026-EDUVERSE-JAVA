package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuestionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class TeacherAjouterQuestionController {

    @FXML
    private TextArea questionTA;

    private final QuestionService questionService = new QuestionService();
    private Quiz selectedQuiz;

    public void setQuiz(Quiz quiz) {
        this.selectedQuiz = quiz;
    }

    @FXML
    public void ajouterQuestion() {
        if (selectedQuiz == null) {
            ControllerUtils.showError("Erreur: Quiz non sélectionné.");
            return;
        }

        if (!ControllerUtils.isTextValid(questionTA)) {
            ControllerUtils.showError("Le texte de la question doit être renseigné et contenir au moins 2 caractères.");
            return;
        }

        Question question = new Question(questionTA.getText().trim(), selectedQuiz.getIdQuiz());

        try {
            questionService.ajouterQuestion(question);
            ControllerUtils.showInfo("Question ajoutée avec succès.");
            // Navigate back to question list with quiz
            goBackToQuestionList();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'ajout de la question : " + e.getMessage());
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
            controller.setQuiz(selectedQuiz);
            
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