package Controllers;

import Entities.Question;
import Entities.Reponse;
import Services.QuestionService;
import Services.ReponseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterReponseController {

    @FXML
    private TextArea reponseTA;

    @FXML
    private TextField scoreTF;

    @FXML
    private ChoiceBox<Question> questionChoice;

    private final ReponseService reponseService = new ReponseService();
    private final QuestionService questionService = new QuestionService();
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
    private Question selectedQuestion;
    private String actorType = "admin";

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public void setSelectedQuestion(Question question) {
        selectedQuestion = question;
        if (questionChoice != null && question != null) {
            questionChoice.setValue(findQuestionById(question.getIdQuestion()));
            questionChoice.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        try {
            List<Question> questions = questionService.afficherQuestion();
            questionChoice.setItems(FXCollections.observableArrayList(questions));
            if (selectedQuestion != null) {
                questionChoice.setValue(findQuestionById(selectedQuestion.getIdQuestion()));
                questionChoice.setDisable(true);
            }
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des questions : " + e.getMessage());
        }
    }

    @FXML
    public void ajouterReponse() {
        if (!ControllerUtils.isTextValid(reponseTA)) {
            ControllerUtils.showError("La reponse doit etre renseignee et contenir au moins 6 caracteres.");
            return;
        }

        if (!ControllerUtils.isDouble(scoreTF)) {
            ControllerUtils.showError("Le score doit etre un nombre valide.");
            return;
        }

        Question question = questionChoice.getValue();
        if (question == null) {
            ControllerUtils.showError("Une question doit etre selectionnee.");
            return;
        }

        double score = Double.parseDouble(scoreTF.getText().trim());
        Reponse reponse = new Reponse(reponseTA.getText().trim(), score, question.getIdQuestion());

        try {
            reponseService.ajouterReponse(reponse);
            ControllerUtils.showInfo("Reponse ajoutee avec succes.");
            goBackToAnswerList(question);
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'ajout de la reponse : " + e.getMessage());
        }
    }

    @FXML
    private void handleReponseSpeech(javafx.event.ActionEvent event) {
        ControllerUtils.startSpeechToText((Button) event.getSource(), reponseTA);
    }

    @FXML
    private void handleReponseGrammar(javafx.event.ActionEvent event) {
        saplingUiSupport.handleGrammarCheck((Button) event.getSource(), reponseTA);
    }

    @FXML
    private void handleReponseTranslateEn(javafx.event.ActionEvent event) {
        translationUiSupport.translateToEnglish((Button) event.getSource(), reponseTA);
    }

    @FXML
    private void handleReponseTranslateFr(javafx.event.ActionEvent event) {
        translationUiSupport.translateToFrench((Button) event.getSource(), reponseTA);
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        Question question = questionChoice.getValue() != null ? questionChoice.getValue() : selectedQuestion;
        goBackToAnswerList(question);
    }

    private Question findQuestionById(int idQuestion) {
        for (Question question : questionChoice.getItems()) {
            if (question.getIdQuestion() == idQuestion) {
                return question;
            }
        }
        Question fallback = new Question();
        fallback.setIdQuestion(idQuestion);
        if (selectedQuestion != null) {
            fallback.setIdQuiz(selectedQuestion.getIdQuiz());
            fallback.setQuestion(selectedQuestion.getQuestion());
        }
        return fallback;
    }

    private void goBackToAnswerList(Question question) {
        try {
            if ("admin".equals(actorType)) {
                FXMLLoader loader = ControllerUtils.createLoader("/admin_answer_list.fxml");
                Parent root = loader.load();
                AdminAnswerListController controller = loader.getController();
                if (question != null) {
                    controller.setQuestion(question);
                }
                switchScene(root);
                return;
            }

            FXMLLoader loader = ControllerUtils.createLoader("/teacher_answer_list.fxml");
            Parent root = loader.load();
            TeacherAnswerListController controller = loader.getController();
            if (question != null) {
                controller.setQuestion(question);
            }
            switchScene(root);
        } catch (IOException | IllegalStateException e) {
            ControllerUtils.showError("Impossible de retourner a la liste des reponses : " + e.getMessage());
        }
    }

    private void switchScene(Parent root) {
        Stage stage = (Stage) reponseTA.getScene().getWindow();
        ControllerUtils.openInApplication(stage, root, "Gestion Reponses");
    }
}
