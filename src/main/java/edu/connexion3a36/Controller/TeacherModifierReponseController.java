package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.services.ReponseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private Reponse selectedReponse;

    public void setReponse(Reponse reponse) {
        this.selectedReponse = reponse;
        if (reponse != null) {
            reponseTA.setText(reponse.getReponse());
            scoreTF.setText(String.valueOf(reponse.getScore()));
        }
    }

    @FXML
    public void modifierReponse() {
        if (selectedReponse == null) {
            ControllerUtils.showWarning("Selectionnez une réponse a modifier.");
            return;
        }

        if (!ControllerUtils.isTextValid(reponseTA)) {
            ControllerUtils.showError("La réponse doit être renseignée.");
            return;
        }

        if (!ControllerUtils.isDouble(scoreTF)) {
            ControllerUtils.showError("Le score doit être un nombre valide.");
            return;
        }

        double score = Double.parseDouble(scoreTF.getText().trim());
        selectedReponse.setReponse(reponseTA.getText().trim());
        selectedReponse.setScore(score);

        try {
            reponseService.modifierReponse(selectedReponse);
            ControllerUtils.showInfo("Réponse modifiée avec succès.");
            goBackToAnswerList();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la modification de la réponse : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        goBackToAnswerList();
    }

    private void goBackToAnswerList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_answer_list.fxml"));
            Parent root = loader.load();
            TeacherAnswerListController controller = loader.getController();
            // Create a temporary Question with just the ID from selectedReponse
            if (selectedReponse != null) {
                edu.connexion3a36.entities.Question tempQuestion = new edu.connexion3a36.entities.Question();
                tempQuestion.setIdQuestion(selectedReponse.getIdQuestion());
                controller.setQuestion(tempQuestion);
            }

            Stage stage = (Stage) reponseTA.getScene().getWindow();
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