package Controllers;

import Entities.Question;
import Entities.Reponse;
import Services.ReponseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class TeacherAjouterReponseController {

    @FXML
    private TextArea reponseTA;

    @FXML
    private TextField scoreTF;

    private final ReponseService reponseService = new ReponseService();
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
    private Question selectedQuestion;

    public void setQuestion(Question question) {
        this.selectedQuestion = question;
    }

    @FXML
    public void ajouterReponse() {
        if (selectedQuestion == null) {
            ControllerUtils.showError("Erreur : question non selectionnee.");
            return;
        }

        if (!ControllerUtils.isTextValid(reponseTA)) {
            ControllerUtils.showError("La reponse doit etre renseignee et contenir au moins 6 caracteres.");
            return;
        }

        if (!ControllerUtils.isDouble(scoreTF)) {
            ControllerUtils.showError("Le score doit etre un nombre valide.");
            return;
        }

        double score = Double.parseDouble(scoreTF.getText().trim());
        Reponse reponse = new Reponse(reponseTA.getText().trim(), score, selectedQuestion.getIdQuestion());

        try {
            reponseService.ajouterReponse(reponse);
            ControllerUtils.showInfo("Reponse ajoutee avec succes.");
            goBackToAnswerList();
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
        goBackToAnswerList();
    }

    private void goBackToAnswerList() {
        try {
            FXMLLoader loader = ControllerUtils.createLoader("/teacher_answer_list.fxml");
            Parent root = loader.load();
            TeacherAnswerListController controller = loader.getController();
            controller.setQuestion(selectedQuestion);

            Stage stage = (Stage) reponseTA.getScene().getWindow();
            ControllerUtils.openInApplication(stage, root, "Gestion Reponses");
        } catch (IOException | IllegalStateException e) {
            ControllerUtils.showError("Impossible de retourner a la liste des reponses : " + e.getMessage());
        }
    }
}
