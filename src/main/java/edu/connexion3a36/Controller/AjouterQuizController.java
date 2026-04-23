package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.services.QuizGenerationService;
import edu.connexion3a36.services.QuizService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AjouterQuizController {

    @FXML
    private TextField titreTF;

    @FXML
    private TextField dureeTF;

    @FXML
    private ChoiceBox<String> levelChoice;

    @FXML
    private TextField nombreQuestionsTF;

    @FXML
    private VBox generatedQuestionsContainer;

    @FXML
    private Label generationStatusLabel;

    @FXML
    private Button saveButton;

    private final QuizService quizService = new QuizService();
    private final QuizGenerationService quizGenerationService = new QuizGenerationService();
    private String actorType = "teacher";
    private final List<QuestionDraft> drafts = new ArrayList<>();

    @FXML
    public void initialize() {
        levelChoice.getItems().setAll("facile", "moyen", "difficile");
        levelChoice.setValue("moyen");
        ControllerUtils.applyQuizTitleFormatter(titreTF);
        saveButton.setDisable(true);
        showEmptyState();
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    @FXML
    public void genererQuiz() {
        String validationMessage = validateForm();
        if (validationMessage != null) {
            ControllerUtils.showError(validationMessage);
            return;
        }

        try {
            Quiz generatedQuiz = quizGenerationService.generateQuiz(
                    titreTF.getText().trim(),
                    Integer.parseInt(dureeTF.getText().trim()),
                    levelChoice.getValue(),
                    Integer.parseInt(nombreQuestionsTF.getText().trim())
            );
            loadDrafts(generatedQuiz);
            generationStatusLabel.setText("Quiz genere. Vous pouvez maintenant modifier, supprimer, completer puis enregistrer.");
            saveButton.setDisable(false);
        } catch (IOException e) {
            generationStatusLabel.setText("Generation impossible.");
            ControllerUtils.showError(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            generationStatusLabel.setText("Generation interrompue.");
            ControllerUtils.showError("La generation IA a ete interrompue.");
        }
    }

    @FXML
    public void ajouterQuiz() {
        String validationMessage = validateForm();
        if (validationMessage != null) {
            ControllerUtils.showError(validationMessage);
            return;
        }
        if (drafts.isEmpty()) {
            ControllerUtils.showError("Generez d abord un quiz ou ajoutez au moins une question manuellement.");
            return;
        }

        Quiz quiz;
        try {
            quiz = buildQuizFromDrafts();
        } catch (IllegalStateException e) {
            ControllerUtils.showError(e.getMessage());
            return;
        }

        String statut = "teacher".equals(actorType) ? "en_attente" : "valide";
        String createdBy = "teacher".equals(actorType) ? "enseignant" : "admin";
        quiz.setStatut(statut);
        quiz.setCreatedBy(createdBy);

        try {
            quizService.ajouterQuizComplet(quiz);
            String message = "teacher".equals(actorType)
                    ? "Quiz genere et enregistre avec succes (en attente de validation)."
                    : "Quiz genere et enregistre avec succes.";
            ControllerUtils.showInfo(message);
            ControllerUtils.navigateTo(titreTF, "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml");
        } catch (SQLException e) {
            if (QuizService.QUIZ_TITLE_ALREADY_EXISTS_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_REQUIRED_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_TOO_SHORT_MESSAGE.equals(e.getMessage())
                    || QuizService.QUIZ_TITLE_INVALID_CHARACTERS_MESSAGE.equals(e.getMessage())) {
                ControllerUtils.showError(e.getMessage());
                return;
            }
            ControllerUtils.showError("Erreur lors de l enregistrement du quiz genere : " + e.getMessage());
        }
    }

    @FXML
    public void addManualQuestion() {
        QuestionDraft draft = new QuestionDraft();
        draft.questionText = "Nouvelle question";
        draft.answers.add(new AnswerDraft("Bonne reponse", 1.0));
        draft.answers.add(new AnswerDraft("Autre reponse", 0.0));
        drafts.add(draft);
        renderDrafts();
        generationStatusLabel.setText("Question ajoutee. Completez ou modifiez le contenu avant enregistrement.");
        saveButton.setDisable(false);
    }

    private void loadDrafts(Quiz quiz) {
        drafts.clear();
        for (Question question : quiz.getQuestions()) {
            QuestionDraft draft = new QuestionDraft();
            draft.questionText = question.getQuestion();
            for (Reponse reponse : question.getReponses()) {
                draft.answers.add(new AnswerDraft(reponse.getReponse(), reponse.getScore()));
            }
            drafts.add(draft);
        }
        renderDrafts();
    }

    private void renderDrafts() {
        generatedQuestionsContainer.getChildren().clear();
        if (drafts.isEmpty()) {
            showEmptyState();
            return;
        }

        for (int i = 0; i < drafts.size(); i++) {
            int questionIndex = i;
            QuestionDraft draft = drafts.get(i);

            VBox card = new VBox(12);
            card.getStyleClass().add("dashboard-card");
            card.setPadding(new Insets(18));

            Label title = new Label("Question " + (i + 1));
            title.getStyleClass().add("card-title");

            Button deleteQuestionButton = new Button("Supprimer");
            deleteQuestionButton.getStyleClass().add("btn-supprimer");
            deleteQuestionButton.setOnAction(event -> {
                drafts.remove(questionIndex);
                renderDrafts();
            });

            HBox header = new HBox(10, title, deleteQuestionButton);
            header.setAlignment(Pos.CENTER_LEFT);

            TextArea questionArea = new TextArea(draft.questionText);
            questionArea.setWrapText(true);
            questionArea.setPromptText("Texte de la question");
            questionArea.setPrefRowCount(3);
            questionArea.textProperty().addListener((obs, oldValue, newValue) -> draft.questionText = newValue);

            Label answersLabel = new Label("Reponses");
            answersLabel.getStyleClass().add("field-label");

            VBox answersBox = new VBox(8);
            for (int j = 0; j < draft.answers.size(); j++) {
                int answerIndex = j;
                AnswerDraft answerDraft = draft.answers.get(j);

                TextField answerField = new TextField(answerDraft.text);
                answerField.setPromptText("Texte de la reponse");
                answerField.textProperty().addListener((obs, oldValue, newValue) -> answerDraft.text = newValue);
                HBox.setHgrow(answerField, Priority.ALWAYS);

                TextField scoreField = new TextField(answerDraft.rawScore);
                scoreField.setPrefWidth(90);
                scoreField.setPromptText("Score");
                scoreField.textProperty().addListener((obs, oldValue, newValue) -> answerDraft.rawScore = newValue);

                Button deleteAnswerButton = new Button("X");
                deleteAnswerButton.getStyleClass().add("btn-secondary");
                deleteAnswerButton.setOnAction(event -> {
                    draft.answers.remove(answerIndex);
                    renderDrafts();
                });

                HBox row = new HBox(8, answerField, scoreField, deleteAnswerButton);
                row.setAlignment(Pos.CENTER_LEFT);
                answersBox.getChildren().add(row);
            }

            Button addAnswerButton = new Button("Ajouter une reponse");
            addAnswerButton.getStyleClass().add("btn-secondary");
            addAnswerButton.setOnAction(event -> {
                draft.answers.add(new AnswerDraft("Nouvelle reponse", 0.0));
                renderDrafts();
            });

            card.getChildren().addAll(header, questionArea, answersLabel, answersBox, addAnswerButton);
            generatedQuestionsContainer.getChildren().add(card);
        }
    }

    private void showEmptyState() {
        Label title = new Label("Aucune question generee");
        title.getStyleClass().add("empty-state-title");
        Label body = new Label("Renseignez le theme, la duree, le niveau et le nombre de questions, puis lancez la generation IA.");
        body.getStyleClass().add("empty-state-text");
        body.setWrapText(true);

        VBox emptyState = new VBox(8, title, body);
        emptyState.getStyleClass().add("empty-state");
        generatedQuestionsContainer.getChildren().setAll(emptyState);
    }

    private String validateForm() {
        String titreErrorMessage = ControllerUtils.getQuizTitleValidationMessage(titreTF);
        if (titreErrorMessage != null) {
            return titreErrorMessage;
        }
        if (!ControllerUtils.isInteger(dureeTF) || Integer.parseInt(dureeTF.getText().trim()) <= 0) {
            return "La duree doit etre un nombre entier positif.";
        }
        if (levelChoice.getValue() == null || levelChoice.getValue().isBlank()) {
            return "Le niveau du quiz doit etre selectionne.";
        }
        if (!ControllerUtils.isInteger(nombreQuestionsTF) || Integer.parseInt(nombreQuestionsTF.getText().trim()) <= 0) {
            return "Le nombre de questions doit etre un entier positif.";
        }
        return null;
    }

    private Quiz buildQuizFromDrafts() {
        Quiz quiz = new Quiz();
        quiz.setTitre(titreTF.getText().trim());
        quiz.setDuree(Integer.parseInt(dureeTF.getText().trim()));
        quiz.setLevel(levelChoice.getValue());

        List<Question> questions = new ArrayList<>();
        for (QuestionDraft draft : drafts) {
            if (draft.questionText == null || draft.questionText.trim().length() < 2) {
                throw new IllegalStateException("Chaque question doit contenir au moins 2 caracteres.");
            }

            Question question = new Question();
            question.setQuestion(draft.questionText.trim());
            List<Reponse> reponses = new ArrayList<>();

            for (AnswerDraft answerDraft : draft.answers) {
                if (answerDraft.text == null || answerDraft.text.trim().isEmpty()) {
                    continue;
                }
                double score;
                try {
                    score = Double.parseDouble(answerDraft.rawScore == null ? String.valueOf(answerDraft.score) : answerDraft.rawScore.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Chaque score doit etre un nombre valide.");
                }
                Reponse reponse = new Reponse();
                reponse.setReponse(answerDraft.text.trim());
                reponse.setScore(score);
                reponses.add(reponse);
            }

            if (reponses.isEmpty()) {
                throw new IllegalStateException("Chaque question doit contenir au moins une reponse.");
            }
            question.setReponses(reponses);
            questions.add(question);
        }

        if (questions.isEmpty()) {
            throw new IllegalStateException("Ajoutez au moins une question avant enregistrement.");
        }

        quiz.setQuestions(questions);
        return quiz;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String targetFxml = "teacher".equals(actorType) ? "/teacher_quiz_list.fxml" : "/admin_quiz_list.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Parent root = loader.load();
            Stage stage = (Stage) titreTF.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner : " + e.getMessage());
        }
    }

    private static class QuestionDraft {
        private String questionText = "";
        private final List<AnswerDraft> answers = new ArrayList<>();
    }

    private static class AnswerDraft {
        private String text;
        private double score;
        private String rawScore;

        private AnswerDraft(String text, double score) {
            this.text = text;
            this.score = score;
            this.rawScore = String.valueOf(score);
        }
    }
}
