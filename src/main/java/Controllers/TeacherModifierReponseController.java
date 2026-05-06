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

public class TeacherModifierReponseController {

    @FXML
    private TextArea reponseTA;

    @FXML
    private TextField scoreTF;

    private final ReponseService reponseService = new ReponseService();
    private final SaplingUiSupport saplingUiSupport = new SaplingUiSupport();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
    private Reponse selectedReponse;

    public void setReponse(Reponse reponse) {
        this.selectedReponse = reponse;
        if (reponse != null && reponseTA != null && scoreTF != null) {
            reponseTA.setText(reponse.getReponse());
            scoreTF.setText(String.valueOf(reponse.getScore()));
        }
    }

    @FXML
    public void modifierReponse() {
        if (selectedReponse == null) {
            ControllerUtils.showWarning("Selectionnez une reponse a modifier.");
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
        selectedReponse.setReponse(reponseTA.getText().trim());
        selectedReponse.setScore(score);

        try {
            reponseService.modifierReponse(selectedReponse);
            ControllerUtils.showInfo("Reponse modifiee avec succes.");
            goBackToAnswerList();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la modification de la reponse : " + e.getMessage());
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
            if (selectedReponse != null) {
                Question tempQuestion = new Question();
                tempQuestion.setIdQuestion(selectedReponse.getIdQuestion());
                controller.setQuestion(tempQuestion);
            }

            Stage stage = (Stage) reponseTA.getScene().getWindow();
            ControllerUtils.openInApplication(stage, root, "Gestion Reponses");
        } catch (IOException | IllegalStateException e) {
            ControllerUtils.showError("Impossible de retourner a la liste des reponses : " + e.getMessage());
        }
    }
}
