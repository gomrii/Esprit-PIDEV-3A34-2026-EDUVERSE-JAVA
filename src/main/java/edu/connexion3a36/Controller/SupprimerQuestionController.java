package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.services.QuestionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class SupprimerQuestionController {

    @FXML
    private TableView<Question> questionTable;

    @FXML
    private TableColumn<Question, Integer> idQuestionCol;

    @FXML
    private TableColumn<Question, String> questionCol;

    @FXML
    private TableColumn<Question, String> correctAnswerCol;

    @FXML
    private TableColumn<Question, Double> scoreCol;

    @FXML
    private TableColumn<Question, String> typeCol;

    private final QuestionService questionService = new QuestionService();

    @FXML
    public void initialize() {
        idQuestionCol.setCellValueFactory(new PropertyValueFactory<>("idQuestion"));
        questionCol.setCellValueFactory(new PropertyValueFactory<>("question"));
        correctAnswerCol.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        chargerQuestions();
    }

    @FXML
    public void supprimerQuestion() {
        Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            ControllerUtils.showWarning("Selectionnez une question a supprimer.");
            return;
        }

        try {
            questionService.supprimerQuestion(selectedQuestion.getIdQuestion());
            ControllerUtils.showInfo("Question supprimee avec succes.");
            chargerQuestions();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la suppression de la question : " + e.getMessage());
        }
    }

    private void chargerQuestions() {
        try {
            questionTable.setItems(FXCollections.observableArrayList(questionService.afficherQuestion()));
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des questions : " + e.getMessage());
        }
    }
}
