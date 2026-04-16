package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuizService;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class AjouterQuizController {

    @FXML
    private TextField titreTF;

    @FXML
    private TextField dureeTF;

    @FXML
    private ChoiceBox<String> levelChoice;

    private final QuizService quizService = new QuizService();
    private String actorType = "teacher";

    @FXML
    public void initialize() {
        levelChoice.getItems().setAll("facile", "moyen", "difficile");
        levelChoice.setValue("moyen");
        ControllerUtils.applyQuizTitleFormatter(titreTF);
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    @FXML
    public void ajouterQuiz() {
        String titreErrorMessage = ControllerUtils.getQuizTitleValidationMessage(titreTF);
        if (titreErrorMessage != null) {
            ControllerUtils.showError(titreErrorMessage);
            return;
        }
        if (!ControllerUtils.isInteger(dureeTF) || Integer.parseInt(dureeTF.getText().trim()) <= 0) {
            ControllerUtils.showError("La duree doit etre un nombre entier positif.");
            return;
        }
        if (levelChoice.getValue() == null || levelChoice.getValue().isBlank()) {
            ControllerUtils.showError("Le niveau du quiz doit etre selectionne.");
            return;
        }

        String statut = "teacher".equals(actorType) ? "en_attente" : "valide";
        String createdBy = "teacher".equals(actorType) ? "enseignant" : "admin";
        Quiz quiz = new Quiz(
                titreTF.getText().trim(),
                statut,
                createdBy,
                Integer.parseInt(dureeTF.getText().trim()),
                levelChoice.getValue()
        );

        try {
            if (quizService.quizTitleExists(quiz.getTitre())) {
                ControllerUtils.showError(QuizService.QUIZ_TITLE_ALREADY_EXISTS_MESSAGE);
                return;
            }
            quizService.ajouterQuiz(quiz);
            String message = "teacher".equals(actorType)
                    ? "Quiz ajoute avec succes (en attente de validation)."
                    : "Quiz ajoute avec succes (valide).";
            ControllerUtils.showInfo(message);
            String targetPage = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            ControllerUtils.navigateTo(titreTF, targetPage);
        } catch (SQLException e) {
            if (QuizService.QUIZ_TITLE_ALREADY_EXISTS_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_REQUIRED_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_TOO_SHORT_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_INVALID_CHARACTERS_MESSAGE.equals(e.getMessage())) {
                ControllerUtils.showError(e.getMessage());
                return;
            }
            ControllerUtils.showError("Erreur lors de l'ajout du quiz : " + e.getMessage());
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
}
