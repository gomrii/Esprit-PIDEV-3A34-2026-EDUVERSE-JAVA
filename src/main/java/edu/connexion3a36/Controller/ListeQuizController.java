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

public class ListeQuizController {

    @FXML
    private TableView<Quiz> quizzesTable;

    @FXML
    private TableColumn<Quiz, Integer> colId;

    @FXML
    private TableColumn<Quiz, String> colTitle;

    @FXML
    private TableColumn<Quiz, String> colStatus;

    @FXML
    private Label selectionLabel;

    private final QuizService quizService = new QuizService();
    private ObservableList<Quiz> masterData = FXCollections.observableArrayList();

    @FXML
    private javafx.scene.control.TextField searchTF;

    @FXML
    private ChoiceBox<String> sortChoice;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idQuiz"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statut"));
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
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(javafx.event.ActionEvent event) {
        applyFiltersAndSort();
    }

    public Quiz getSelectedQuiz() {
        return quizzesTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        openFXML(event, "/quiz_add.fxml");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (getSelectedQuiz() == null) {
            showAlert("Info", "Sélectionnez un quiz à modifier.");
            return;
        }
        openFXML(event, "/quiz_edit.fxml");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (getSelectedQuiz() == null) {
            showAlert("Info", "Sélectionnez un quiz à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le quiz sélectionné ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                quizService.supprimerQuiz(getSelectedQuiz().getIdQuiz());
                ControllerUtils.showInfo("Quiz supprimé.");
                chargerQuiz();
            } catch (SQLException e) {
                ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleValidate(ActionEvent event) {
        if (getSelectedQuiz() == null) { showAlert("Info", "Sélectionnez un quiz à valider."); return; }
        try {
            quizService.validerQuiz(getSelectedQuiz().getIdQuiz());
            ControllerUtils.showInfo("Quiz validé.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la validation : " + e.getMessage());
        }
    }

    @FXML
    private void handleReject(ActionEvent event) {
        if (getSelectedQuiz() == null) { showAlert("Info", "Sélectionnez un quiz à rejeter."); return; }
        try {
            quizService.rejeterQuiz(getSelectedQuiz().getIdQuiz());
            ControllerUtils.showInfo("Quiz rejeté.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du rejet : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        chargerQuiz();
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
            showAlert("Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }

    private void openFXML(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            // If the controller has a setQuiz method, pass the selected quiz
            try {
                if (controller != null) {
                    java.lang.reflect.Method m = null;
                    try { m = controller.getClass().getMethod("setQuiz", Quiz.class); } catch (NoSuchMethodException ignored) {}
                    if (m != null) m.invoke(controller, getSelectedQuiz());
                }
            } catch (Exception ignored) {}
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Quiz> filtered = FXCollections.observableArrayList();
        for (Quiz quiz : masterData) {
            if (q.isEmpty()
                    || String.valueOf(quiz.getIdQuiz()).contains(q)
                    || (quiz.getTitre() != null && quiz.getTitre().toLowerCase().contains(q))
                    || (quiz.getStatut() != null && quiz.getStatut().toLowerCase().contains(q))) {
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
}
