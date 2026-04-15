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

public class TeacherQuizListController {

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
        
        // Listen to table selection changes to update button states
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
            // Clear selection and reset button states
            quizzesTable.getSelectionModel().clearSelection();
            updateButtonStates(null);
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }

    /**
     * Update button states based on quiz creator
     * If quiz is created by "admin", disable modification buttons for teacher
     */
    private void updateButtonStates(Quiz selectedQuiz) {
        boolean noSelection = selectedQuiz == null;
        boolean adminOwned = selectedQuiz != null && "admin".equalsIgnoreCase(selectedQuiz.getCreatedBy());
        boolean allowTeacherActions = selectedQuiz != null && !adminOwned;

        btnModify.setDisable(!allowTeacherActions);
        btnDelete.setDisable(!allowTeacherActions);
        btnManageQuestions.setDisable(!allowTeacherActions);

        if (noSelection) {
            btnModify.setVisible(true);
            btnDelete.setVisible(true);
            btnManageQuestions.setVisible(true);
            btnModify.setManaged(true);
            btnDelete.setManaged(true);
            btnManageQuestions.setManaged(true);
            return;
        }

        btnModify.setVisible(!adminOwned);
        btnDelete.setVisible(!adminOwned);
        btnManageQuestions.setVisible(!adminOwned);
        btnModify.setManaged(!adminOwned);
        btnDelete.setManaged(!adminOwned);
        btnManageQuestions.setManaged(!adminOwned);
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
            controller.setActorType("teacher");
            
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
            ControllerUtils.showWarning("Sélectionnez un quiz à modifier.");
            return;
        }
        
        // Check if teacher can edit this quiz (only if created by teacher)
        if ("admin".equals(selected.getCreatedBy())) {
            ControllerUtils.showWarning("Ce quiz a été créé par l'admin. Vous ne pouvez pas le modifier.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz_edit.fxml"));
            Parent root = loader.load();
            ModifierQuizController controller = loader.getController();
            controller.setQuiz(selected);
            controller.setActorType("teacher");
            
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
            ControllerUtils.showWarning("Sélectionnez un quiz à supprimer.");
            return;
        }
        
        // Check if teacher can delete this quiz
        if ("admin".equals(selected.getCreatedBy())) {
            ControllerUtils.showWarning("Ce quiz a été créé par l'admin. Vous ne pouvez pas le supprimer.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le quiz sélectionné ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                quizService.supprimerQuiz(selected.getIdQuiz());
                ControllerUtils.showInfo("Quiz supprimé.");
                chargerQuiz();
            } catch (SQLException e) {
                ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleManageQuestions(ActionEvent event) {
        Quiz selected = getSelectedQuiz();
        if (selected == null) {
            ControllerUtils.showWarning("Sélectionnez un quiz pour gérer ses questions.");
            return;
        }
        
        // Check if teacher can manage questions of this quiz
        if ("admin".equals(selected.getCreatedBy())) {
            ControllerUtils.showWarning("Ce quiz a été créé par l'admin. Vous ne pouvez pas gérer ses questions.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_question_list.fxml"));
            Parent root = loader.load();
            TeacherQuestionListController controller = loader.getController();
            controller.setQuiz(selected);
            
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
            ControllerUtils.showError("Impossible d'ouvrir la gestion des questions: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        chargerQuiz();
        updateButtonStates(null);
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
