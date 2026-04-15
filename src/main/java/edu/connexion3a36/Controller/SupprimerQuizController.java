package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuizService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class SupprimerQuizController {

    @FXML
    private TableView<Quiz> quizTable;

    @FXML
    private TableColumn<Quiz, Integer> idQuizCol;

    @FXML
    private TableColumn<Quiz, String> titreCol;

    @FXML
    private TableColumn<Quiz, String> statutCol;

    private final QuizService quizService = new QuizService();

    @FXML
    public void initialize() {
        idQuizCol.setCellValueFactory(new PropertyValueFactory<>("idQuiz"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        chargerQuiz();
    }

    @FXML
    public void supprimerQuiz() {
        Quiz selectedQuiz = quizTable.getSelectionModel().getSelectedItem();
        if (selectedQuiz == null) {
            ControllerUtils.showWarning("Selectionnez un quiz a supprimer.");
            return;
        }

        try {
            quizService.supprimerQuiz(selectedQuiz.getIdQuiz());
            ControllerUtils.showInfo("Quiz supprime avec succes.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la suppression du quiz : " + e.getMessage());
        }
    }

    private void chargerQuiz() {
        try {
            quizTable.setItems(FXCollections.observableArrayList(quizService.afficherQuiz()));
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }
}
