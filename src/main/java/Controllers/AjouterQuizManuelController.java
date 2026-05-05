package Controllers;

import Entities.Quiz;
import Services.QuizService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterQuizManuelController {

    @FXML
    private TextField titreTF;

    @FXML
    private TextField dureeTF;

    @FXML
    private ChoiceBox<String> levelChoice;

    private final QuizService quizService = new QuizService();
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
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

        Quiz quiz = new Quiz();
        quiz.setTitre(titreTF.getText().trim());
        quiz.setDuree(Integer.parseInt(dureeTF.getText().trim()));
        quiz.setLevel(levelChoice.getValue());
        quiz.setStatut("teacher".equals(actorType) ? "en_attente" : "valide");
        quiz.setCreatedBy(ControllerUtils.buildQuizCreatorKey());

        try {
            quizService.ajouterQuiz(quiz);
            ControllerUtils.showInfo("Quiz ajoute avec succes.");
            ControllerUtils.navigateTo(titreTF, "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml");
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
    private void handleTitreSpeech(javafx.event.ActionEvent event) {
        ControllerUtils.startSpeechToText((Button) event.getSource(), titreTF);
    }

    @FXML
    private void handleTitreGrammar(javafx.event.ActionEvent event) {
        saplingUiSupport.handleGrammarCheck((Button) event.getSource(), titreTF);
    }

    @FXML
    private void handleTitreTranslateEn(javafx.event.ActionEvent event) {
        translationUiSupport.translateToEnglish((Button) event.getSource(), titreTF);
    }

    @FXML
    private void handleTitreTranslateFr(javafx.event.ActionEvent event) {
        translationUiSupport.translateToFrench((Button) event.getSource(), titreTF);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String targetFxml = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Parent root = loader.load();
            Stage stage = (Stage) titreTF.getScene().getWindow();
            ControllerUtils.openInApplication(stage, root, null);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner : " + e.getMessage());
        }
    }
}
