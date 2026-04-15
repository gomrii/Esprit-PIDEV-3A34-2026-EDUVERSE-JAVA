package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.services.ReponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.sql.SQLException;

public class AdminAnswerListController {

    @FXML
    private TableView<Reponse> reponsesTable;

    @FXML
    private TableColumn<Reponse, Integer> colId;

    @FXML
    private TableColumn<Reponse, String> colReponse;

    @FXML
    private TableColumn<Reponse, Double> colScore;

    @FXML
    private Label questionLabel;

    @FXML
    private Button btnModify;

    @FXML
    private Button btnDelete;

    @FXML
    private TextField searchTF;

    @FXML
    private ChoiceBox<String> sortChoice;

    private final ReponseService reponseService = new ReponseService();
    private ObservableList<Reponse> masterData = FXCollections.observableArrayList();
    private Question selectedQuestion;
    private int questionId = -1;

    public void setQuestion(Question question) {
        selectedQuestion = question;
        questionId = question != null ? question.getIdQuestion() : -1;
        if (question != null) {
            String label = question.getQuestion() != null && !question.getQuestion().isBlank()
                    ? "Réponses de la question: " + question.getQuestion()
                    : "Réponses de la question (ID: " + question.getIdQuestion() + ")";
            questionLabel.setText(label);
            chargerReponses();
        }
    }

    public void setQuestionId(int idQuestion) {
        questionId = idQuestion;
        selectedQuestion = null;
        questionLabel.setText("Réponses de la question (ID: " + idQuestion + ")");
        chargerReponses();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReponse"));
        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponse"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        reponsesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonStates(newVal);
        });
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID décroissant",
                "Réponse A-Z",
                "Réponse Z-A",
                "Score croissant",
                "Score décroissant"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        updateButtonStates(null);
    }

    public void chargerReponses() {
        if (questionId < 0) {
            return;
        }
        try {
            masterData = FXCollections.observableArrayList(reponseService.afficherReponsesByIdQuestion(questionId));
            applyFiltersAndSort();
            reponsesTable.getSelectionModel().clearSelection();
            updateButtonStates(null);
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des réponses : " + e.getMessage());
        }
    }

    private void updateButtonStates(Reponse selectedReponse) {
        boolean disableActions = selectedReponse == null;
        btnModify.setDisable(disableActions);
        btnDelete.setDisable(disableActions);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    public Reponse getSelectedReponse() {
        return reponsesTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (questionId < 0) {
            ControllerUtils.showWarning("Sélectionnez une question avant d'ajouter une réponse.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/answer_add.fxml"));
            Parent root = loader.load();
            AjouterReponseController controller = loader.getController();
            controller.setActorType("admin");
            controller.setSelectedQuestion(selectedQuestion != null ? selectedQuestion : buildQuestionContext());

            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        Reponse selected = getSelectedReponse();
        if (selected == null) {
            ControllerUtils.showWarning("Sélectionnez une réponse à modifier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/answer_edit.fxml"));
            Parent root = loader.load();
            ModifierReponseController controller = loader.getController();
            controller.setActorType("admin");
            controller.setContextQuestion(selectedQuestion != null ? selectedQuestion : buildQuestionContext());
            controller.setReponse(selected);

            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Reponse selected = getSelectedReponse();
        if (selected == null) {
            ControllerUtils.showWarning("Sélectionnez une réponse à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la réponse sélectionnée ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                reponseService.supprimerReponse(selected.getIdReponse());
                ControllerUtils.showInfo("Réponse supprimée.");
                chargerReponses();
            } catch (SQLException e) {
                ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        chargerReponses();
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Reponse> filtered = FXCollections.observableArrayList();
        for (Reponse item : masterData) {
            if (q.isEmpty()
                    || String.valueOf(item.getIdReponse()).contains(q)
                    || (item.getReponse() != null && item.getReponse().toLowerCase().contains(q))
                    || String.valueOf(item.getScore()).contains(q)) {
                filtered.add(item);
            }
        }

        Comparator<Reponse> comparator = Comparator.comparingInt(Reponse::getIdReponse);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID décroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Reponse::getIdReponse).reversed();
        } else if ("Réponse A-Z".equals(sort)) {
            comparator = Comparator.comparing(reponse -> safeValue(reponse.getReponse()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Réponse Z-A".equals(sort)) {
            comparator = Comparator.comparing((Reponse reponse) -> safeValue(reponse.getReponse()), String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Score croissant".equals(sort)) {
            comparator = Comparator.comparingDouble(Reponse::getScore).thenComparingInt(Reponse::getIdReponse);
        } else if ("Score décroissant".equals(sort)) {
            comparator = Comparator.comparingDouble(Reponse::getScore).reversed().thenComparingInt(Reponse::getIdReponse);
        }

        FXCollections.sort(filtered, comparator);
        reponsesTable.setItems(filtered);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_question_list.fxml"));
            Parent root = loader.load();
            AdminQuestionListController controller = loader.getController();
            int parentQuizId = selectedQuestion != null ? selectedQuestion.getIdQuiz() : -1;
            if (parentQuizId >= 0) {
                controller.setQuizId(parentQuizId);
            }

            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner aux questions: " + e.getMessage());
        }
    }

    private Question buildQuestionContext() {
        Question question = new Question();
        question.setIdQuestion(questionId);
        if (selectedQuestion != null) {
            question.setIdQuiz(selectedQuestion.getIdQuiz());
        }
        return question;
    }

    private void switchScene(ActionEvent event, Parent root) {
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
    }
}
