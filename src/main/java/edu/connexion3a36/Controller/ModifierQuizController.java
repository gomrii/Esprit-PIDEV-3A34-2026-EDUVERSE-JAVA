package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuizService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class ModifierQuizController {

    @FXML
    private Label idLabel;

    @FXML
    private TextField titreTF;

    @FXML
    private Label statutLabel;

    @FXML
    private Label createdByLabel;

    @FXML
    private Button saveButton;

    private final QuizService quizService = new QuizService();
    private Quiz selectedQuiz;
    private String actorType = "teacher";

    public void setActorType(String actorType) {
        this.actorType = actorType;
        applyPermissions();
    }

    public void setQuiz(Quiz quiz) {
        selectedQuiz = quiz;
        if (quiz != null) {
            idLabel.setText(String.valueOf(quiz.getIdQuiz()));
            titreTF.setText(quiz.getTitre());
            statutLabel.setText(quiz.getStatut());
            createdByLabel.setText(quiz.getCreatedBy());
        }
        applyPermissions();
    }

    @FXML
    public void modifierQuiz() {
        if (selectedQuiz == null) {
            ControllerUtils.showWarning("Sélectionnez un quiz à modifier.");
            return;
        }

        if ("teacher".equals(actorType) && "admin".equalsIgnoreCase(selectedQuiz.getCreatedBy())) {
            ControllerUtils.showWarning("Ce quiz a été créé par l'admin. Il est en lecture seule pour l'enseignant.");
            return;
        }

        if (!ControllerUtils.isTextValid(titreTF)) {
            ControllerUtils.showError("Le titre doit être rempli et contenir au moins 6 caractères.");
            return;
        }

        selectedQuiz.setTitre(titreTF.getText().trim());

        try {
            quizService.modifierQuiz(selectedQuiz);
            ControllerUtils.showInfo("Quiz modifié avec succès.");
            String targetPage = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            ControllerUtils.navigateTo(idLabel, targetPage);
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la modification du quiz : " + e.getMessage());
        }
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

    private void applyPermissions() {
        if (titreTF == null || saveButton == null) {
            return;
        }

        boolean readOnlyForTeacher = "teacher".equals(actorType)
                && selectedQuiz != null
                && "admin".equalsIgnoreCase(selectedQuiz.getCreatedBy());

        titreTF.setDisable(readOnlyForTeacher);
        saveButton.setDisable(readOnlyForTeacher);
    }
}
