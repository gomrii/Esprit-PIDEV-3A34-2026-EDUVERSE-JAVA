package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
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

public class TeacherAjouterReponseController {

    @FXML
    private TextArea reponseTA;

    @FXML
    private TextField scoreTF;

    private final ReponseService reponseService = new ReponseService();
    private Question selectedQuestion;

    public void setQuestion(Question question) {
        this.selectedQuestion = question;
    }

    @FXML
    public void ajouterReponse() {
        if (selectedQuestion == null) {
            ControllerUtils.showError("Erreur: Question non sélectionnée.");
            return;
        }

        if (!ControllerUtils.isTextValid(reponseTA)) {
            ControllerUtils.showError("La réponse doit être renseignée et contenir au moins 2 caractères.");
            return;
        }

        if (!ControllerUtils.isDouble(scoreTF)) {
            ControllerUtils.showError("Le score doit être un nombre valide.");
            return;
        }

        double score = Double.parseDouble(scoreTF.getText().trim());
        Reponse reponse = new Reponse(reponseTA.getText().trim(), score, selectedQuestion.getIdQuestion());

        try {
            reponseService.ajouterReponse(reponse);
            ControllerUtils.showInfo("Réponse ajoutée avec succès.");
            goBackToAnswerList();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'ajout de la réponse : " + e.getMessage());
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
            controller.setQuestion(selectedQuestion);

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