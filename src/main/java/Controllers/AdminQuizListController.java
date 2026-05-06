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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;

public class AdminQuizListController {

    @FXML
    private VBox cardsContainer;
    @FXML
    private TextField searchTF;
    @FXML
    private ChoiceBox<String> sortChoice;
    @FXML
    private ChoiceBox<String> filterChoice;
    @FXML
    private Label summaryLabel;
    @FXML
    private Button accessibilityButton;
    @FXML
    private Label totalQuizValueLabel;
    @FXML
    private Label validatedQuizValueLabel;
    @FXML
    private Label pendingQuizValueLabel;
    @FXML
    private Label rejectedQuizValueLabel;

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

        filterChoice.setItems(FXCollections.observableArrayList(
                "Tous statuts",
                "Valide",
                "En attente",
                "Rejete"
        ));
        filterChoice.setValue("Tous statuts");

        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        filterChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        searchTF.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        ControllerUtils.configureAccessibilityButton(accessibilityButton);
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
    private void handleAddManual(ActionEvent event) {
        try {
            FXMLLoader loader = ControllerUtils.createLoader("/quiz_add_manual.fxml");
            Parent root = loader.load();
            AjouterQuizManuelController controller = loader.getController();
            controller.setActorType("admin");
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire d'ajout manuel: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddAi(ActionEvent event) {
        try {
            FXMLLoader loader = ControllerUtils.createLoader("/quiz_add.fxml");
            Parent root = loader.load();
            AjouterQuizController controller = loader.getController();
            controller.setActorType("admin");
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire de generation IA: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchTF != null) {
            searchTF.clear();
        }
        if (filterChoice != null) {
            filterChoice.setValue("Tous statuts");
        }
        chargerQuiz();
    }

    @FXML
    private void handleOpenStatistics(ActionEvent event) {
        try {
            Parent root = ControllerUtils.loadFxml("/admin_stats.fxml");
            switchScene(event, root);
        } catch (IOException | IllegalStateException e) {
            ControllerUtils.showError("Impossible d'ouvrir la page statistiques : " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenChatbot(ActionEvent event) {
        ChatbotController.openDialog((Node) event.getSource());
    }

    @FXML
    private void handleToggleAccessibility(ActionEvent event) {
        if (ControllerUtils.isAccessibilityActive()) {
            ControllerUtils.deactivateAccessibility(accessibilityButton);
        } else {
            ControllerUtils.activateAccessibility(accessibilityButton);
        }
    }

    private void openQuizEditor(Quiz quiz, ActionEvent event) {
        try {
            FXMLLoader loader = ControllerUtils.createLoader("/quiz_edit.fxml");
            Parent root = loader.load();
            ModifierQuizController controller = loader.getController();
            controller.setQuiz(quiz);
            controller.setActorType("admin");
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    private void deleteQuiz(Quiz quiz) {
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
        try {
            FXMLLoader loader = ControllerUtils.createLoader("/admin_question_list.fxml");
            Parent root = loader.load();
            AdminQuestionListController controller = loader.getController();
            controller.setQuiz(quiz);
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la consultation des questions: " + e.getMessage());
        }
    }

    private void validateQuiz(Quiz quiz) {
        try {
            quizService.validerQuiz(quiz.getIdQuiz());
            chargerQuiz();
            ControllerUtils.showInfo("Quiz accepte.");
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors de l'acceptation : " + e.getMessage());
        }
    }

    private void rejectQuiz(Quiz quiz) {
        try {
            quizService.rejeterQuiz(quiz.getIdQuiz());
            chargerQuiz();
            ControllerUtils.showInfo("Quiz rejete.");
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du rejet : " + e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        String query = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        String statusFilter = filterChoice == null ? "tous statuts" : safeValue(filterChoice.getValue()).toLowerCase();
        ObservableList<Quiz> filtered = FXCollections.observableArrayList();

        for (Quiz quiz : masterData) {
            boolean matchesSearch = query.isEmpty()
                    || String.valueOf(quiz.getIdQuiz()).contains(query)
                    || safeValue(quiz.getTitre()).toLowerCase().contains(query)
                    || safeValue(quiz.getStatut()).toLowerCase().contains(query)
                    || safeValue(quiz.getCreatedBy()).toLowerCase().contains(query)
                    || safeValue(quiz.getLevel()).toLowerCase().contains(query)
                    || String.valueOf(quiz.getDuree()).contains(query);

            boolean matchesStatus = "tous statuts".equals(statusFilter)
                    || ("valide".equals(statusFilter) && ControllerUtils.isValidatedQuizStatus(quiz.getStatut()))
                    || ("en attente".equals(statusFilter) && ControllerUtils.isPendingQuizStatus(quiz.getStatut()))
                    || ("rejete".equals(statusFilter) && ControllerUtils.isRejectedQuizStatus(quiz.getStatut()));

            if (matchesSearch && matchesStatus) {
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
        updateStats(quizzes);

        if (quizzes.isEmpty()) {
            cardsContainer.getChildren().add(createEmptyState(
                    "Aucun quiz trouve",
                    "Essayez un autre filtre ou ajoutez un nouveau quiz."
            ));
            return;
        }

        for (Quiz quiz : quizzes) {
            cardsContainer.getChildren().add(createQuizCard(quiz));
        }
    }

    private void updateStats(ObservableList<Quiz> quizzes) {
        if (totalQuizValueLabel == null || validatedQuizValueLabel == null || pendingQuizValueLabel == null || rejectedQuizValueLabel == null) {
            return;
        }

        int total = quizzes.size();
        int validated = 0;
        int pending = 0;
        int rejected = 0;

        for (Quiz quiz : quizzes) {
            if (ControllerUtils.isValidatedQuizStatus(quiz.getStatut())) {
                validated++;
            } else if (ControllerUtils.isPendingQuizStatus(quiz.getStatut())) {
                pending++;
            } else if (ControllerUtils.isRejectedQuizStatus(quiz.getStatut())) {
                rejected++;
            }
        }

        totalQuizValueLabel.setText(String.valueOf(total));
        validatedQuizValueLabel.setText(String.valueOf(validated));
        pendingQuizValueLabel.setText(String.valueOf(pending));
        rejectedQuizValueLabel.setText(String.valueOf(rejected));
    }

    private VBox createQuizCard(Quiz quiz) {
        VBox card = new VBox(16);
        card.getStyleClass().addAll("dashboard-card", "quiz-card");
        card.setPadding(new Insets(20));

        Label title = new Label(safeValue(quiz.getTitre()));
        title.getStyleClass().add("quiz-card-title");
        title.setWrapText(true);

        Label creator = new Label("Createur : " + ControllerUtils.formatQuizCreator(quiz.getCreatedBy()));
        creator.getStyleClass().add("quiz-card-subtitle");

        Label summary = new Label("Duree : " + quiz.getDuree() + " min"
                + "  |  Niveau : " + ControllerUtils.formatQuizLevel(quiz.getLevel())
                + "  |  Questions : " + getQuestionCountLabel(quiz));
        summary.getStyleClass().add("quiz-card-meta");
        summary.setWrapText(true);

        VBox headerText = new VBox(6, title, creator, summary);
        headerText.getStyleClass().add("quiz-card-main");

        HBox topRow = new HBox(18, createQuizVisualIcon(quiz), headerText, createStatusBadge(quiz.getStatut()));
        topRow.getStyleClass().add("quiz-card-header");
        topRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        FlowPane metaRow = new FlowPane(10, 10,
                createInfoBox("ID", String.valueOf(quiz.getIdQuiz())),
                createInfoBox("Createur", ControllerUtils.formatQuizCreator(quiz.getCreatedBy())),
                createInfoBox("Duree", quiz.getDuree() + " min"),
                createInfoBox("Niveau", ControllerUtils.formatQuizLevel(quiz.getLevel()))
        );
        metaRow.getStyleClass().add("quiz-info-grid");

        Button manageQuestions = new Button("Questions");
        ControllerUtils.decorateButton(manageQuestions, "M4,5H20V7H4V5M4,11H20V13H4V11M4,17H14V19H4V17Z", "btn-secondary", "btn-questions", "card-action-button");
        manageQuestions.setOnAction(event -> manageQuestions(quiz, event));

        Button modify = new Button("Modifier");
        ControllerUtils.decorateButton(modify, "M3,17.25V21H6.75L17.81,9.94L14.06,6.19L3,17.25M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.13,5.12L18.88,8.87L20.71,7.04Z", "btn-modifier", "btn-edit", "card-action-button");
        modify.setOnAction(event -> openQuizEditor(quiz, event));

        Button delete = new Button("Supprimer");
        ControllerUtils.decorateButton(delete, "M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19M8,9H16V19H8V9M15.5,4L14.5,3H9.5L8.5,4H5V6H19V4H15.5Z", "btn-supprimer", "btn-delete", "card-action-button");
        delete.setOnAction(event -> deleteQuiz(quiz));

        Button accept = new Button("Accepter");
        ControllerUtils.decorateButton(accept, "M9,16.17L4.83,12L3.41,13.41L9,19L21,7L19.59,5.59L9,16.17Z", "btn-success", "btn-accept", "card-action-button");
        accept.setDisable(isStatusTreated(quiz));
        accept.setOnAction(event -> validateQuiz(quiz));

        Button reject = new Button("Rejeter");
        ControllerUtils.decorateButton(reject, "M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z", "warning-button", "btn-reject", "card-action-button");
        reject.setDisable(isStatusTreated(quiz));
        reject.setOnAction(event -> rejectQuiz(quiz));

        HBox actions = new HBox(10, manageQuestions, modify, delete, accept, reject);
        actions.getStyleClass().add("quiz-actions");

        card.getChildren().addAll(topRow, metaRow, actions);
        return card;
    }

    private Label createStatusBadge(String status) {
        Label badge = new Label(ControllerUtils.formatQuizStatus(status));
        badge.getStyleClass().addAll("status-badge", "quiz-status-badge", statusStyle(status));
        return badge;
    }

    private VBox createInfoBox(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("quiz-info-label");
        Label value = new Label(valueText == null || valueText.isBlank() ? "-" : valueText);
        value.getStyleClass().add("quiz-info-value");

        VBox chip = new VBox(4, label, value);
        chip.getStyleClass().add("quiz-info-box");
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

    private String displayValue(String value) {
        String safe = safeValue(value).trim();
        return safe.isEmpty() ? "-" : safe;
    }

    private String getQuestionCountLabel(Quiz quiz) {
        return quiz.getQuestions() == null || quiz.getQuestions().isEmpty() ? "-" : String.valueOf(quiz.getQuestions().size());
    }

    private boolean isStatusTreated(Quiz quiz) {
        return ControllerUtils.isValidatedQuizStatus(quiz.getStatut())
                || ControllerUtils.isRejectedQuizStatus(quiz.getStatut());
    }

    private StackPane createQuizVisualIcon(Quiz quiz) {
        SVGPath glyph = new SVGPath();
        glyph.setContent(resolveQuizGlyph(quiz));
        glyph.getStyleClass().add("quiz-card-icon-glyph");

        StackPane iconWrap = new StackPane(glyph);
        iconWrap.getStyleClass().addAll("quiz-card-icon", resolveQuizAccent(quiz));
        return iconWrap;
    }

    private String resolveQuizGlyph(Quiz quiz) {
        String title = safeValue(quiz.getTitre()).toLowerCase();
        if (title.contains("java")) {
            return "M9,3C11,5 12,7 12,8.5A3.5,3.5 0 0,1 8.5,12C7.1,12 6,10.9 6,9.5C6,7.8 7.2,6.5 9,3M14,4C15.8,6.2 17,7.8 17,9.5A5,5 0 0,1 12,14.5A4.65,4.65 0 0,1 9.6,13.9C10.5,14.9 12,16.5 12,18.5A3.5,3.5 0 0,1 8.5,22A3.5,3.5 0 0,1 5,18.5C5,16.5 6.7,15 8,14C5.7,13 4,10.9 4,8.5C4,5.8 6.1,3.7 8.8,3.7C10.9,3.7 12.5,4.8 14,4Z";
        }
        if (title.contains("sql") || title.contains("database") || title.contains("mysql")) {
            return "M12,3C7,3 3,4.79 3,7V17C3,19.21 7,21 12,21C17,21 21,19.21 21,17V7C21,4.79 17,3 12,3M12,5C16.42,5 19,6.43 19,7C19,7.57 16.42,9 12,9C7.58,9 5,7.57 5,7C5,6.43 7.58,5 12,5M5,10.19C6.76,11.31 9.33,12 12,12C14.67,12 17.24,11.31 19,10.19V13C19,13.57 16.42,15 12,15C7.58,15 5,13.57 5,13V10.19M5,16.19C6.76,17.31 9.33,18 12,18C14.67,18 17.24,17.31 19,16.19V17C19,17.57 16.42,19 12,19C7.58,19 5,17.57 5,17V16.19Z";
        }
        if (title.contains("reseau") || title.contains("réseau") || title.contains("network")
                || title.contains("tcp") || title.contains("ip") || title.contains("routing")
                || title.contains("switch") || title.contains("router")) {
            return "M4,10A2,2 0 0,1 6,8A2,2 0 0,1 8,10A2,2 0 0,1 6,12A2,2 0 0,1 4,10M16,6A2,2 0 0,1 18,4A2,2 0 0,1 20,6A2,2 0 0,1 18,8A2,2 0 0,1 16,6M16,18A2,2 0 0,1 18,16A2,2 0 0,1 20,18A2,2 0 0,1 18,20A2,2 0 0,1 16,18M7.5,9H12V7H14V9H16.5V11H14V13H12V11H7.5V9M14,10H16.5M7.5,10H10M14,7V6.5C14,5.12 15.12,4 16.5,4H17M14,13V17.5C14,18.33 14.67,19 15.5,19H17";
        }
        if (title.contains("html") || title.contains("css") || title.contains("web")) {
            return "M4,4H20L18.2,18.59L12,21L5.8,18.59L4,4M8.5,7L9,10H15L14.8,12H9.2L9.5,15L12,15.9L14.5,15L14.7,13H16.9L16.5,16.5L12,18L7.5,16.5L6.9,11H14.6L14.8,9H6.7L6.3,7H8.5Z";
        }
        return "M12,3L1,9L5,11V17L12,21L19,17V11L21,10V17H23V9L12,3M18.82,9L12,12.72L5.18,9L12,5.28L18.82,9M17,15.8L12,18.5L7,15.8V12.08L12,14.8L17,12.08V15.8Z";
    }

    private String resolveQuizAccent(Quiz quiz) {
        String title = safeValue(quiz.getTitre()).toLowerCase();
        if (title.contains("java")) {
            return "quiz-card-accent-orange";
        }
        if (title.contains("sql") || title.contains("database") || title.contains("mysql")) {
            return "quiz-card-accent-blue";
        }
        if (title.contains("reseau") || title.contains("réseau") || title.contains("network")
                || title.contains("tcp") || title.contains("ip") || title.contains("routing")
                || title.contains("switch") || title.contains("router")) {
            return "quiz-card-accent-indigo";
        }
        if (title.contains("html") || title.contains("css") || title.contains("web")) {
            return "quiz-card-accent-violet";
        }
        return "quiz-card-accent-teal";
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (MainDashboardController.getInstance() != null) {
            MainDashboardController.getInstance().loadView(
                    ControllerUtils.getRoleBasedDashboardFxml(),
                    ControllerUtils.getRoleBasedDashboardTitle()
            );
            return;
        }
        ControllerUtils.navigateTo(
                (Node) event.getSource(),
                ControllerUtils.getRoleBasedDashboardFxml(),
                "Impossible de retourner au dashboard."
        );
    }

    private void switchScene(ActionEvent event, Parent root) {
        Node source = (Node) event.getSource();
        if (source.getScene() == null || source.getScene().getWindow() == null) {
            throw new IllegalStateException("Scene ou fenetre JavaFX introuvable.");
        }
        Stage stage = (Stage) source.getScene().getWindow();
        ControllerUtils.openInApplication(stage, root, null);
    }
}
