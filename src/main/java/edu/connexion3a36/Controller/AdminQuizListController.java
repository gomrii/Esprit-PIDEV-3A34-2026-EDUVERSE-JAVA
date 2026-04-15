package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuizService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.sql.SQLException;

public class AdminQuizListController {

    @FXML
    private TableView<Quiz> quizzesTable;

    @FXML
    private TableColumn<Quiz, Integer> colId;

    @FXML
    private TableColumn<Quiz, String> colTitle;

    @FXML
    private TableColumn<Quiz, String> colStatus;

    @FXML
    private TableColumn<Quiz, String> colCreatedBy;

    @FXML
    private Button btnModify;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnManageQuestions;

    private final QuizService quizService = new QuizService();
    private ObservableList<Quiz> masterData = FXCollections.observableArrayList();

    @FXML
    private TextField searchTF;

    @FXML
    private ChoiceBox<String> sortChoice;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idQuiz"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdBy"));

        quizzesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonStates(newVal);
        });
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID décroissant",
                "Titre A-Z",
                "Titre Z-A",
                "Statut"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());

        chargerQuiz();
    }

    public void chargerQuiz() {
        try {
            masterData = FXCollections.observableArrayList(quizService.afficherQuiz());
            applyFiltersAndSort();
            quizzesTable.getSelectionModel().clearSelection();
            updateButtonStates(null);
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }

    private void updateButtonStates(Quiz selectedQuiz) {
        boolean disableActions = selectedQuiz == null;
        btnModify.setDisable(disableActions);
        btnDelete.setDisable(disableActions);
        btnManageQuestions.setDisable(disableActions);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    public Quiz getSelectedQuiz() {
        return quizzesTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz_add.fxml"));
            Parent root = loader.load();
            AjouterQuizController controller = loader.getController();
            controller.setActorType("admin");
            
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        Quiz selected = getSelectedQuiz();
        if (selected == null) {
            ControllerUtils.showWarning("SÃ©lectionnez un quiz Ã  modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz_edit.fxml"));
            Parent root = loader.load();
            ModifierQuizController controller = loader.getController();
            controller.setQuiz(selected);
            controller.setActorType("admin");

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Quiz selected = getSelectedQuiz();
        if (selected == null) {
            ControllerUtils.showWarning("SÃ©lectionnez un quiz Ã  supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le quiz sÃ©lectionnÃ© ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                quizService.supprimerQuiz(selected.getIdQuiz());
                ControllerUtils.showInfo("Quiz supprimÃ©.");
                chargerQuiz();
            } catch (SQLException e) {
                ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleValidate(ActionEvent event) {
        Quiz selected = getSelectedQuiz();
        if (selected == null) {
            ControllerUtils.showWarning("Sélectionnez un quiz à accepter.");
            return;
        }
        
        try {
            quizService.validerQuiz(selected.getIdQuiz());
            ControllerUtils.showInfo("Quiz accepté.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'acceptation : " + e.getMessage());
        }
    }

    @FXML
    private void handleReject(ActionEvent event) {
        Quiz selected = getSelectedQuiz();
        if (selected == null) {
            ControllerUtils.showWarning("Sélectionnez un quiz à rejeter.");
            return;
        }
        
        try {
            quizService.rejeterQuiz(selected.getIdQuiz());
            ControllerUtils.showInfo("Quiz rejeté.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du rejet : " + e.getMessage());
        }
    }

    @FXML
    private void handleManageQuestions(ActionEvent event) {
        if (getSelectedQuiz() == null) {
            ControllerUtils.showWarning("Sélectionnez un quiz pour consulter ses questions.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_question_list.fxml"));
            Parent root = loader.load();
            AdminQuestionListController controller = loader.getController();
            controller.setQuiz(getSelectedQuiz());
            
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la consultation des questions: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        chargerQuiz();
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Quiz> filtered = FXCollections.observableArrayList();
        for (Quiz quiz : masterData) {
            if (q.isEmpty()
                    || String.valueOf(quiz.getIdQuiz()).contains(q)
                    || (quiz.getTitre() != null && quiz.getTitre().toLowerCase().contains(q))
                    || (quiz.getStatut() != null && quiz.getStatut().toLowerCase().contains(q))
                    || (quiz.getCreatedBy() != null && quiz.getCreatedBy().toLowerCase().contains(q))) {
                filtered.add(quiz);
            }
        }

        Comparator<Quiz> comparator = Comparator.comparingInt(Quiz::getIdQuiz);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID décroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Quiz::getIdQuiz).reversed();
        } else if ("Titre A-Z".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getTitre()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Titre Z-A".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getTitre()), String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Statut".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getStatut()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(Quiz::getIdQuiz);
        }

        FXCollections.sort(filtered, comparator);
        quizzesTable.setItems(filtered);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }
}
