package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.services.QuestionService;
import edu.connexion3a36.services.ReponseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ModifierReponseController {

    @FXML
    private TextArea reponseTA;

    @FXML
    private TextField scoreTF;

    @FXML
    private ChoiceBox<Question> questionChoice;

    private final ReponseService reponseService = new ReponseService();
    private final QuestionService questionService = new QuestionService();
    private Reponse selectedReponse;
    private Question contextQuestion;
    private String actorType = "admin";

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public void setContextQuestion(Question question) {
        contextQuestion = question;
    }

    @FXML
    public void initialize() {
        try {
            List<Question> questions = questionService.afficherQuestion();
            questionChoice.setItems(FXCollections.observableArrayList(questions));
            applySelection();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des questions : " + e.getMessage());
        }
    }

    public void setReponse(Reponse reponse) {
        selectedReponse = reponse;
        applySelection();
    }

    @FXML
    public void modifierReponse() {
        if (selectedReponse == null) {
            ControllerUtils.showWarning("Sélectionnez une réponse à modifier.");
            return;
        }
        if (!ControllerUtils.isTextValid(reponseTA)) {
            ControllerUtils.showError("La réponse doit être renseignée et contenir au moins 6 caractères.");
            return;
        }
        if (!ControllerUtils.isDouble(scoreTF)) {
            ControllerUtils.showError("Le score doit être un nombre valide.");
            return;
        }
        Question question = questionChoice.getValue();
        if (question == null) {
            ControllerUtils.showError("Une question doit être sélectionnée.");
            return;
        }

        double score = Double.parseDouble(scoreTF.getText().trim());
        selectedReponse.setReponse(reponseTA.getText().trim());
        selectedReponse.setScore(score);
        selectedReponse.setIdQuestion(question.getIdQuestion());

        try {
            reponseService.modifierReponse(selectedReponse);
            ControllerUtils.showInfo("Réponse modifiée avec succès.");
            goBackToAnswerList(question);
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la modification de la réponse : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(javafx.event.ActionEvent event) {
        handleBack(event);
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        goBackToAnswerList(contextQuestion != null ? contextQuestion : findQuestionById(selectedReponse != null ? selectedReponse.getIdQuestion() : -1));
    }

    private void applySelection() {
        if (reponseTA == null || scoreTF == null || questionChoice == null || selectedReponse == null) {
            return;
        }
        reponseTA.setText(selectedReponse.getReponse());
        scoreTF.setText(String.valueOf(selectedReponse.getScore()));
        questionChoice.setValue(findQuestionById(selectedReponse.getIdQuestion()));
        if ("admin".equals(actorType) && contextQuestion != null) {
            questionChoice.setDisable(true);
        }
    }

    private Question findQuestionById(int idQuestion) {
        for (Question question : questionChoice.getItems()) {
            if (question.getIdQuestion() == idQuestion) {
                return question;
            }
        }
        Question fallback = new Question();
        fallback.setIdQuestion(idQuestion);
        if (contextQuestion != null) {
            fallback.setIdQuiz(contextQuestion.getIdQuiz());
            fallback.setQuestion(contextQuestion.getQuestion());
        }
        return fallback;
    }

    private void goBackToAnswerList(Question question) {
        try {
            if ("admin".equals(actorType)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_answer_list.fxml"));
                Parent root = loader.load();
                AdminAnswerListController controller = loader.getController();
                if (question != null) {
                    controller.setQuestion(question);
                }
                switchScene(root);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/answer_list.fxml"));
            Parent root = loader.load();
            switchScene(root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner : " + e.getMessage());
        }
    }

    private void switchScene(Parent root) {
        Stage stage = (Stage) reponseTA.getScene().getWindow();
        Scene scene = stage.getScene();
        if (scene == null) {
            Scene newScene = new Scene(root, 1100, 700);
            newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(newScene);
        } else {
            scene.setRoot(root);
        }
    }
}
