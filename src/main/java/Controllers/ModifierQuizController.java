package Controllers;

import Entities.Quiz;
import Services.QuizService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class ModifierQuizController {

    @FXML
    private Label idLabel;

    @FXML
    private TextField titreTF;

    @FXML
    private TextField dureeTF;

    @FXML
    private ChoiceBox<String> levelChoice;

    @FXML
    private Label statutLabel;

    @FXML
    private Label createdByLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button titreGrammarButton;

    @FXML
    private Button titreTranslateEnButton;

    @FXML
    private Button titreTranslateFrButton;

    private final QuizService quizService = new QuizService();
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
    private Quiz selectedQuiz;
    private String actorType = "teacher";

    @FXML
    public void initialize() {
        levelChoice.getItems().setAll("facile", "moyen", "difficile");
        ControllerUtils.applyQuizTitleFormatter(titreTF);
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
        applyPermissions();
    }

    public void setQuiz(Quiz quiz) {
        selectedQuiz = quiz;
        if (quiz != null) {
            idLabel.setText(String.valueOf(quiz.getIdQuiz()));
            titreTF.setText(quiz.getTitre());
            dureeTF.setText(String.valueOf(quiz.getDuree()));
            levelChoice.setValue(quiz.getLevel());
            statutLabel.setText(ControllerUtils.formatQuizStatus(quiz.getStatut()));
            createdByLabel.setText(ControllerUtils.formatQuizCreator(quiz.getCreatedBy()));
        }
        applyPermissions();
    }

    @FXML
    public void modifierQuiz() {
        if (selectedQuiz == null) {
            ControllerUtils.showWarning("Selectionnez un quiz a modifier.");
            return;
        }

        if ("teacher".equals(actorType) && ControllerUtils.isAdminOwnedQuiz(selectedQuiz)) {
            ControllerUtils.showWarning("Ce quiz a ete cree par l'admin. Il est en lecture seule pour l'enseignant.");
            return;
        }

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

        selectedQuiz.setTitre(titreTF.getText().trim());
        selectedQuiz.setDuree(Integer.parseInt(dureeTF.getText().trim()));
        selectedQuiz.setLevel(levelChoice.getValue());

        try {
            if (quizService.quizTitleExistsForAnotherQuiz(selectedQuiz.getTitre(), selectedQuiz.getIdQuiz())) {
                ControllerUtils.showError(QuizService.QUIZ_TITLE_ALREADY_EXISTS_MESSAGE);
                return;
            }
            quizService.modifierQuiz(selectedQuiz);
            ControllerUtils.showInfo("Quiz modifie avec succes.");
            String targetPage = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            ControllerUtils.navigateTo(idLabel, targetPage);
        } catch (SQLException e) {
            if (QuizService.QUIZ_TITLE_ALREADY_EXISTS_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_REQUIRED_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_TOO_SHORT_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_INVALID_CHARACTERS_MESSAGE.equals(e.getMessage())) {
                ControllerUtils.showError(e.getMessage());
                return;
            }
            ControllerUtils.showError("Erreur lors de la modification du quiz : " + e.getMessage());
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
    private void handleBack(javafx.event.ActionEvent event) {
        try {
            String targetFxml = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(targetFxml));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
            ControllerUtils.openInApplication(stage, root, null);
        } catch (java.io.IOException e) {
            ControllerUtils.showError("Impossible de retourner : " + e.getMessage());
        }
    }

    private void applyPermissions() {
        if (titreTF == null || dureeTF == null || levelChoice == null || saveButton == null) {
            return;
        }

        boolean readOnlyForTeacher = "teacher".equals(actorType)
                && selectedQuiz != null
                && ControllerUtils.isAdminOwnedQuiz(selectedQuiz);

        titreTF.setDisable(readOnlyForTeacher);
        dureeTF.setDisable(readOnlyForTeacher);
        levelChoice.setDisable(readOnlyForTeacher);
        saveButton.setDisable(readOnlyForTeacher);
        if (titreGrammarButton != null) {
            titreGrammarButton.setDisable(readOnlyForTeacher);
        }
        if (titreTranslateEnButton != null) {
            titreTranslateEnButton.setDisable(readOnlyForTeacher);
        }
        if (titreTranslateFrButton != null) {
            titreTranslateFrButton.setDisable(readOnlyForTeacher);
        }
    }
}
