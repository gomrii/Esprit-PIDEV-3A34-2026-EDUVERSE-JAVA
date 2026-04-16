package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.QuizService;
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

public class TeacherQuizListController {

    @FXML
    private VBox cardsContainer;
    @FXML
    private TextField searchTF;
    @FXML
    private ChoiceBox<String> sortChoice;
    @FXML
    private Label summaryLabel;

    private final QuizService quizService = new QuizService();
    private ObservableList<Quiz> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID decroissant",
                "Titre A-Z",
                "Titre Z-A",
                "Statut",
                "Niveau",
                "Duree croissante",
                "Duree decroissante"
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
            ControllerUtils.showError("Erreur lors du chargement des quiz : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz_add.fxml"));
            Parent root = loader.load();
            AjouterQuizController controller = loader.getController();
            controller.setActorType("teacher");
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
        chargerQuiz();
    }

    private void openQuizEditor(Quiz quiz, ActionEvent event) {
        if ("admin".equalsIgnoreCase(quiz.getCreatedBy())) {
            ControllerUtils.showWarning("Ce quiz a ete cree par l admin. Vous ne pouvez pas le modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz_edit.fxml"));
            Parent root = loader.load();
            ModifierQuizController controller = loader.getController();
            controller.setQuiz(quiz);
            controller.setActorType("teacher");
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    private void deleteQuiz(Quiz quiz) {
        if ("admin".equalsIgnoreCase(quiz.getCreatedBy())) {
            ControllerUtils.showWarning("Ce quiz a ete cree par l admin. Vous ne pouvez pas le supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le quiz selectionne ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) {
            return;
        }

        try {
            quizService.supprimerQuiz(quiz.getIdQuiz());
            ControllerUtils.showInfo("Quiz supprime.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void manageQuestions(Quiz quiz, ActionEvent event) {
        if ("admin".equalsIgnoreCase(quiz.getCreatedBy())) {
            ControllerUtils.showWarning("Ce quiz a ete cree par l admin. Vous ne pouvez pas gerer ses questions.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teacher_question_list.fxml"));
            Parent root = loader.load();
            TeacherQuestionListController controller = loader.getController();
            controller.setQuiz(quiz);
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la gestion des questions: " + e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Quiz> filtered = FXCollections.observableArrayList();
        for (Quiz quiz : masterData) {
            if (q.isEmpty()
                    || String.valueOf(quiz.getIdQuiz()).contains(q)
                    || safeValue(quiz.getTitre()).toLowerCase().contains(q)
                    || safeValue(quiz.getStatut()).toLowerCase().contains(q)
                    || safeValue(quiz.getCreatedBy()).toLowerCase().contains(q)
                    || safeValue(quiz.getLevel()).toLowerCase().contains(q)
                    || String.valueOf(quiz.getDuree()).contains(q)) {
                filtered.add(quiz);
            }
        }

        Comparator<Quiz> comparator = Comparator.comparingInt(Quiz::getIdQuiz);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID decroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Quiz::getIdQuiz).reversed();
        } else if ("Titre A-Z".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getTitre()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Titre Z-A".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getTitre()), String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Statut".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getStatut()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(Quiz::getIdQuiz);
        } else if ("Niveau".equals(sort)) {
            comparator = Comparator.comparing((Quiz quiz) -> safeValue(quiz.getLevel()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(Quiz::getIdQuiz);
        } else if ("Duree croissante".equals(sort)) {
            comparator = Comparator.comparingInt(Quiz::getDuree).thenComparingInt(Quiz::getIdQuiz);
        } else if ("Duree decroissante".equals(sort)) {
            comparator = Comparator.comparingInt(Quiz::getDuree).reversed().thenComparingInt(Quiz::getIdQuiz);
        }

        FXCollections.sort(filtered, comparator);
        renderQuizCards(filtered);
    }

    private void renderQuizCards(ObservableList<Quiz> quizzes) {
        cardsContainer.getChildren().clear();
        summaryLabel.setText(quizzes.size() + (quizzes.size() > 1 ? " quiz affiches" : " quiz affiche"));

        if (quizzes.isEmpty()) {
            cardsContainer.getChildren().add(createEmptyState(
                    "Aucun quiz disponible",
                    "Les quiz apparaitront ici avec leurs actions integrees."
            ));
            return;
        }

        for (Quiz quiz : quizzes) {
            cardsContainer.getChildren().add(createQuizCard(quiz));
        }
    }

    private VBox createQuizCard(Quiz quiz) {
        VBox card = new VBox(16);
        card.getStyleClass().add("dashboard-card");
        card.setPadding(new Insets(18));

        Label overline = new Label("Quiz #" + quiz.getIdQuiz());
        overline.getStyleClass().add("card-overline");

        Label title = new Label(safeValue(quiz.getTitre()));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        HBox topRow = new HBox(12, new VBox(6, overline, title), createStatusBadge(safeValue(quiz.getStatut())));
        topRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(topRow.getChildren().get(0), Priority.ALWAYS);

        HBox metaRow = new HBox(10,
                createMetaChip("Createur", safeValue(quiz.getCreatedBy())),
                createMetaChip("Duree", quiz.getDuree() + " min"),
                createMetaChip("Niveau", safeValue(quiz.getLevel()))
        );

        boolean adminOwned = "admin".equalsIgnoreCase(quiz.getCreatedBy());

        Button manageQuestions = new Button("Gerer Questions");
        manageQuestions.getStyleClass().add("btn-secondary");
        manageQuestions.setDisable(adminOwned);
        manageQuestions.setOnAction(event -> manageQuestions(quiz, event));

        Button modify = new Button("Modifier");
        modify.getStyleClass().add("btn-modifier");
        modify.setDisable(adminOwned);
        modify.setOnAction(event -> openQuizEditor(quiz, event));

        Button delete = new Button("Supprimer");
        delete.getStyleClass().add("btn-supprimer");
        delete.setDisable(adminOwned);
        delete.setOnAction(event -> deleteQuiz(quiz));

        HBox actions = new HBox(10, manageQuestions, modify, delete);
        actions.getStyleClass().add("card-actions");

        if (adminOwned) {
            Label readOnly = new Label("Lecture seule pour l enseignant");
            readOnly.getStyleClass().add("inline-note");
            actions.getChildren().add(readOnly);
        }

        card.getChildren().addAll(topRow, metaRow, actions);
        return card;
    }

    private Label createStatusBadge(String status) {
        Label badge = new Label(status.isBlank() ? "Sans statut" : status);
        badge.getStyleClass().addAll("status-badge", statusStyle(status));
        return badge;
    }

    private VBox createMetaChip(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("meta-label");
        Label value = new Label(valueText == null || valueText.isBlank() ? "-" : valueText);
        value.getStyleClass().add("meta-value");

        VBox chip = new VBox(4, label, value);
        chip.getStyleClass().add("meta-chip");
        return chip;
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

    private String statusStyle(String status) {
        String normalized = safeValue(status).toLowerCase();
        if (normalized.contains("valid")) {
            return "status-valid";
        }
        if (normalized.contains("rejet")) {
            return "status-rejected";
        }
        if (normalized.contains("attente")) {
            return "status-pending";
        }
        return "status-neutral";
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner a l'accueil: " + e.getMessage());
        }
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
