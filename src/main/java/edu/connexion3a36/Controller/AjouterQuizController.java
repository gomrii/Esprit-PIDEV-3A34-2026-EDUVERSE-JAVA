package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuizService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class AjouterQuizController {

    @FXML
    private TextField titreTF;

    private final QuizService quizService = new QuizService();
    private String actorType = "teacher"; // "teacher" or "admin"

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    @FXML
    public void ajouterQuiz() {
        if (!ControllerUtils.isTextValid(titreTF)) {
            ControllerUtils.showError("Le titre doit être rempli et contenir au moins 6 caractères.");
            return;
        }

        // Set status and createdBy based on actor type
        String statut = "teacher".equals(actorType) ? "en_attente" : "valide";
        String createdBy = "teacher".equals(actorType) ? "enseignant" : "admin";
        Quiz quiz = new Quiz(titreTF.getText().trim(), statut, createdBy);

        try {
            quizService.ajouterQuiz(quiz);
            String message = "teacher".equals(actorType)
                    ? "Quiz ajouté avec succès (en attente de validation)."
                    : "Quiz ajouté avec succès (validé).";
            ControllerUtils.showInfo(message);
            // Navigate back to list
            String targetPage = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            ControllerUtils.navigateTo(titreTF, targetPage);
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'ajout du quiz : " + e.getMessage());
        }
    }

    private void clearFields() {
        titreTF.clear();
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        try {
            String targetFxml = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(targetFxml));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
            javafx.scene.Scene scene = stage.getScene();
            if (scene == null) {
                javafx.scene.Scene newScene = new javafx.scene.Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (java.io.IOException e) {
            ControllerUtils.showError("Impossible de retourner : " + e.getMessage());
        }
    }
}