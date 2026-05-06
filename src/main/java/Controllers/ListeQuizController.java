package Controllers;

import Entities.Quiz;
import Services.QuizService;
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

public class ListeQuizController {

    @FXML
    private VBox cardsContainer;
    @FXML
    private Label summaryLabel;
    @FXML
    private TextField searchTF;
    @FXML
    private ChoiceBox<String> sortChoice;

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
        searchTF.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
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
        openQuizForm(event, null, "/quiz_add.fxml");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchTF != null) {
            searchTF.clear();
        }
        chargerQuiz();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner a l'accueil: " + e.getMessage());
        }
    }

    private void handleEdit(Quiz quiz, ActionEvent event) {
        openQuizForm(event, quiz, "/quiz_edit.fxml");
    }

    private void handleDelete(Quiz quiz) {
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

    private void handleValidate(Quiz quiz) {
        try {
            quizService.validerQuiz(quiz.getIdQuiz());
            ControllerUtils.showInfo("Quiz valide.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de la validation : " + e.getMessage());
        }
    }

    private void handleReject(Quiz quiz) {
        try {
            quizService.rejeterQuiz(quiz.getIdQuiz());
            ControllerUtils.showInfo("Quiz rejete.");
            chargerQuiz();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du rejet : " + e.getMessage());
        }
    }

    private void openQuizForm(ActionEvent event, Quiz quiz, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (quiz != null && controller != null) {
                try {
                    controller.getClass().getMethod("setQuiz", Quiz.class).invoke(controller, quiz);
                } catch (Exception ignored) {
                }
            }
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la page: " + e.getMessage());
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
            cardsContainer.getChildren().add(createEmptyState("Aucun quiz trouve", "Les resultats filtres apparaitront ici."));
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

        HBox header = new HBox(12, new VBox(6, overline, title), createStatusBadge(quiz.getStatut()));
        HBox.setHgrow(header.getChildren().get(0), Priority.ALWAYS);

        HBox metaRow = new HBox(10,
                createMetaChip("Duree", quiz.getDuree() + " min"),
                createMetaChip("Niveau", ControllerUtils.formatQuizLevel(quiz.getLevel()))
        );
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Button validate = new Button("Valider");
        validate.getStyleClass().add("btn-success");
        validate.setOnAction(event -> handleValidate(quiz));

        Button reject = new Button("Rejeter");
        reject.getStyleClass().add("warning-button");
        reject.setOnAction(event -> handleReject(quiz));

        Button edit = new Button("Modifier");
        edit.getStyleClass().add("btn-modifier");
        edit.setOnAction(event -> handleEdit(quiz, event));

        Button delete = new Button("Supprimer");
        delete.getStyleClass().add("btn-supprimer");
        delete.setOnAction(event -> handleDelete(quiz));

        HBox actions = new HBox(10, validate, reject, edit, delete);
        actions.getStyleClass().add("card-actions");

        card.getChildren().addAll(header, metaRow, actions);
        return card;
    }

    private Label createStatusBadge(String status) {
        Label badge = new Label(ControllerUtils.formatQuizStatus(status));
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
        if (ControllerUtils.isValidatedQuizStatus(status)) {
            return "status-valid";
        }
        if (ControllerUtils.isRejectedQuizStatus(status)) {
            return "status-rejected";
        }
        if (ControllerUtils.isPendingQuizStatus(status)) {
            return "status-pending";
        }
        return "status-neutral";
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private void switchScene(ActionEvent event, Parent root) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
