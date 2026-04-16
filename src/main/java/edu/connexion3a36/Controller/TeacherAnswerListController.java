package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.services.ReponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;

public class TeacherAnswerListController {

    @FXML
    private VBox cardsContainer;
    @FXML
    private Label questionLabel;
    @FXML
    private TextField searchTF;
    @FXML
    private ChoiceBox<String> sortChoice;
    @FXML
    private Label summaryLabel;

    private final ReponseService reponseService = new ReponseService();
    private ObservableList<Reponse> masterData = FXCollections.observableArrayList();
    private Question selectedQuestion;

    public void setQuestion(Question question) {
        selectedQuestion = question;
        if (question != null) {
            questionLabel.setText("Reponses de la question: " + question.getQuestion());
            chargerReponses();
        }
    }

    @FXML
    public void initialize() {
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID decroissant",
                "Reponse A-Z",
                "Reponse Z-A",
                "Score croissant",
                "Score decroissant"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    public void chargerReponses() {
        if (selectedQuestion == null) {
            return;
        }
        try {
            masterData = FXCollections.observableArrayList(reponseService.afficherReponsesByIdQuestion(selectedQuestion.getIdQuestion()));
            applyFiltersAndSort();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des reponses : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_answer_add.fxml"));
            Parent root = loader.load();
            TeacherAjouterReponseController controller = loader.getController();
            controller.setQuestion(selectedQuestion);
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchTF != null) {
            searchTF.clear();
        }
        chargerReponses();
    }

    private void editAnswer(Reponse reponse, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_answer_edit.fxml"));
            Parent root = loader.load();
            TeacherModifierReponseController controller = loader.getController();
            controller.setReponse(reponse);
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    private void deleteAnswer(Reponse reponse) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la reponse selectionnee ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) {
            return;
        }
        try {
            reponseService.supprimerReponse(reponse.getIdReponse());
            ControllerUtils.showInfo("Reponse supprimee.");
            chargerReponses();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_question_list.fxml"));
            Parent root = loader.load();
            TeacherQuestionListController controller = loader.getController();
            if (selectedQuestion != null) {
                controller.setQuizId(selectedQuestion.getIdQuiz());
            }
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner aux questions: " + e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Reponse> filtered = FXCollections.observableArrayList();
        for (Reponse item : masterData) {
            if (q.isEmpty()
                    || String.valueOf(item.getIdReponse()).contains(q)
                    || safeValue(item.getReponse()).toLowerCase().contains(q)
                    || String.valueOf(item.getScore()).contains(q)) {
                filtered.add(item);
            }
        }

        Comparator<Reponse> comparator = Comparator.comparingInt(Reponse::getIdReponse);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID decroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Reponse::getIdReponse).reversed();
        } else if ("Reponse A-Z".equals(sort)) {
            comparator = Comparator.comparing(reponse -> safeValue(reponse.getReponse()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Reponse Z-A".equals(sort)) {
            comparator = Comparator.comparing((Reponse reponse) -> safeValue(reponse.getReponse()), String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Score croissant".equals(sort)) {
            comparator = Comparator.comparingDouble(Reponse::getScore).thenComparingInt(Reponse::getIdReponse);
        } else if ("Score decroissant".equals(sort)) {
            comparator = Comparator.comparingDouble(Reponse::getScore).reversed().thenComparingInt(Reponse::getIdReponse);
        }

        FXCollections.sort(filtered, comparator);
        renderAnswerCards(filtered);
    }

    private void renderAnswerCards(ObservableList<Reponse> reponses) {
        cardsContainer.getChildren().clear();
        summaryLabel.setText(reponses.size() + (reponses.size() > 1 ? " reponses affichees" : " reponse affichee"));

        if (reponses.isEmpty()) {
            cardsContainer.getChildren().add(createEmptyState(
                    "Aucune reponse disponible",
                    "Ajoutez des reponses pour completer cette question."
            ));
            return;
        }

        for (Reponse reponse : reponses) {
            cardsContainer.getChildren().add(createAnswerCard(reponse));
        }
    }

    private VBox createAnswerCard(Reponse reponse) {
        VBox card = new VBox(16);
        card.getStyleClass().add("dashboard-card");
        card.setPadding(new Insets(18));

        Label overline = new Label("Reponse #" + reponse.getIdReponse());
        overline.getStyleClass().add("card-overline");

        Label title = new Label(safeValue(reponse.getReponse()));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        HBox header = new HBox(12, new VBox(6, overline, title), createStatusBadge("Score " + reponse.getScore(), "status-valid"));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);
        header.setAlignment(Pos.TOP_LEFT);

        Button edit = new Button("Modifier");
        edit.getStyleClass().add("btn-modifier");
        edit.setOnAction(event -> editAnswer(reponse, event));

        Button delete = new Button("Supprimer");
        delete.getStyleClass().add("btn-supprimer");
        delete.setOnAction(event -> deleteAnswer(reponse));

        HBox actions = new HBox(10, edit, delete);
        actions.getStyleClass().add("card-actions");

        card.getChildren().addAll(header, actions);
        return card;
    }

    private Label createStatusBadge(String text, String styleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("status-badge", styleClass);
        return badge;
    }

    private VBox createEmptyState(String titleText, String bodyText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("empty-state-title");
        Label body = new Label(bodyText);
        body.getStyleClass().add("empty-state-text");
        body.setWrapText(true);

        VBox box = new VBox(8, title, body);
        box.getStyleClass().add("empty-state");
        return box;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
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
