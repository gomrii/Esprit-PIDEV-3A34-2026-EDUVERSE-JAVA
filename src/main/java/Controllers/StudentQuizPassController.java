package Controllers;

import Entities.Question;
import Entities.Quiz;
import Entities.Reponse;
import Services.QuestionService;
import Services.ReponseService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentQuizPassController {

    @FXML
    private Label quizTitleLabel;
    @FXML
    private Label progressLabel;
    @FXML
    private Label metaLabel;
    @FXML
    private Label timerLabel;
    @FXML
    private Label questionLabel;
    @FXML
    private VBox answersBox;
    @FXML
    private Button translateEnButton;
    @FXML
    private Button translateFrButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button submitButton;

    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    private final GeminiTranslationUiSupport translationUiSupport = new GeminiTranslationUiSupport();
    private final Map<Integer, List<Reponse>> responsesByQuestion = new HashMap<>();
    private final Map<Integer, Integer> selectedReponses = new HashMap<>();

    private Quiz selectedQuiz;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private ToggleGroup answersGroup;
    private Timeline countdownTimeline;
    private int remainingSeconds;
    private boolean quizFinished = false;
    private boolean timeExpired = false;

    public void setQuiz(Quiz quiz) {
        selectedQuiz = quiz;
        if (quizTitleLabel != null && quiz != null) {
            quizTitleLabel.setText("Passage du quiz : " + quiz.getTitre());
            metaLabel.setText("Niveau : " + safeValue(quiz.getLevel()) + " | Duree : " + quiz.getDuree() + " min");
            startTimer(quiz.getDuree());
        }
        loadQuizContent();
    }

    private void loadQuizContent() {
        if (selectedQuiz == null || quizTitleLabel == null) {
            return;
        }

        try {
            questions = questionService.afficherQuestionsByIdQuiz(selectedQuiz.getIdQuiz());
            responsesByQuestion.clear();
            for (Question question : questions) {
                responsesByQuestion.put(
                        question.getIdQuestion(),
                        reponseService.afficherReponsesByIdQuestion(question.getIdQuestion())
                );
            }
            currentQuestionIndex = 0;
            renderCurrentQuestion();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement du quiz : " + e.getMessage());
        }
    }

    @FXML
    private void handlePrevious(javafx.event.ActionEvent event) {
        if (quizFinished) {
            return;
        }
        saveCurrentSelection();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            renderCurrentQuestion();
        }
    }

    @FXML
    private void handleNext(javafx.event.ActionEvent event) {
        if (quizFinished) {
            return;
        }
        if (!hasSelectionForCurrentQuestion()) {
            ControllerUtils.showWarning("Veuillez selectionner une reponse avant de continuer.");
            return;
        }
        saveCurrentSelection();
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            renderCurrentQuestion();
        }
    }

    @FXML
    private void handleSubmit(javafx.event.ActionEvent event) {
        if (quizFinished) {
            return;
        }
        submitQuiz(event, false);
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        stopTimer();
        ControllerUtils.navigateTo((Node) event.getSource(), "/student_quiz_list.fxml");
    }

    @FXML
    private void handleTranslateEn(javafx.event.ActionEvent event) {
        translateCurrentQuestionAndAnswers((Button) event.getSource(), true);
    }

    @FXML
    private void handleTranslateFr(javafx.event.ActionEvent event) {
        translateCurrentQuestionAndAnswers((Button) event.getSource(), false);
    }

    private void submitQuiz(javafx.event.ActionEvent event, boolean expiredByTimer) {
        if (quizFinished && !expiredByTimer) {
            return;
        }

        saveCurrentSelection();
        if (questions.isEmpty()) {
            ControllerUtils.showWarning("Ce quiz ne contient aucune question.");
            return;
        }

        if (!expiredByTimer) {
            int unansweredCount = countUnansweredQuestions();
            if (unansweredCount > 0) {
                Alert warning = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Certaines questions ne sont pas encore repondues.\nVoulez-vous vraiment soumettre le quiz ?",
                        ButtonType.YES,
                        ButtonType.NO
                );
                warning.setTitle("Confirmation");
                warning.setHeaderText(null);
                warning.showAndWait();
                if (warning.getResult() != ButtonType.YES) {
                    return;
                }
            } else {
                Alert confirm = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Voulez-vous vraiment soumettre le quiz ?",
                        ButtonType.YES,
                        ButtonType.NO
                );
                confirm.setTitle("Confirmation");
                confirm.setHeaderText(null);
                confirm.showAndWait();
                if (confirm.getResult() != ButtonType.YES) {
                    return;
                }
            }
        }

        quizFinished = true;
        timeExpired = expiredByTimer;
        lockQuizInteractions();
        stopTimer();
        showResult(event);
    }

    private void showResult(javafx.event.ActionEvent event) {
        double score = 0;
        double totalPossibleScore = 0;
        int answeredCount = 0;

        for (Question question : questions) {
            double questionMaxScore = 0;
            for (Reponse reponse : responsesByQuestion.getOrDefault(question.getIdQuestion(), List.of())) {
                questionMaxScore = Math.max(questionMaxScore, reponse.getScore());
            }
            totalPossibleScore += questionMaxScore;

            Integer selectedReponseId = selectedReponses.get(question.getIdQuestion());
            if (selectedReponseId == null) {
                continue;
            }

            answeredCount++;
            for (Reponse reponse : responsesByQuestion.getOrDefault(question.getIdQuestion(), List.of())) {
                if (reponse.getIdReponse() == selectedReponseId) {
                    score += reponse.getScore();
                    break;
                }
            }
        }

        int totalQuestions = questions.size();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/student_quiz_result.fxml"));
            Parent root = loader.load();
            StudentQuizResultController controller = loader.getController();
            controller.setResult(selectedQuiz, score, totalPossibleScore, answeredCount, totalQuestions, timeExpired);
            switchScene(event, root);
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'afficher le resultat : " + e.getMessage());
        }
    }

    private void renderCurrentQuestion() {
        if (questions.isEmpty()) {
            progressLabel.setText("Aucune question");
            questionLabel.setText("Ce quiz ne contient aucune question.");
            answersBox.getChildren().clear();
            if (translateEnButton != null) {
                translateEnButton.setDisable(true);
            }
            if (translateFrButton != null) {
                translateFrButton.setDisable(true);
            }
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            submitButton.setDisable(false);
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        progressLabel.setText("Question " + (currentQuestionIndex + 1) + " / " + questions.size());
        questionLabel.setText(question.getQuestion());
        if (translateEnButton != null) {
            translateEnButton.setDisable(false);
        }
        if (translateFrButton != null) {
            translateFrButton.setDisable(false);
        }

        answersGroup = new ToggleGroup();
        answersBox.getChildren().clear();
        for (Reponse reponse : responsesByQuestion.getOrDefault(question.getIdQuestion(), List.of())) {
            RadioButton radioButton = new RadioButton(reponse.getReponse());
            radioButton.setUserData(reponse.getIdReponse());
            radioButton.setToggleGroup(answersGroup);
            radioButton.setWrapText(true);
            radioButton.setDisable(quizFinished);
            radioButton.getStyleClass().add("student-answer-option");
            answersBox.getChildren().add(radioButton);

            Integer selectedId = selectedReponses.get(question.getIdQuestion());
            if (selectedId != null && selectedId == reponse.getIdReponse()) {
                radioButton.setSelected(true);
            }
        }

        prevButton.setDisable(quizFinished || currentQuestionIndex == 0);
        nextButton.setDisable(quizFinished || currentQuestionIndex == questions.size() - 1);
        submitButton.setDisable(quizFinished);
        answersBox.setDisable(quizFinished);
    }

    private void saveCurrentSelection() {
        if (questions.isEmpty() || answersGroup == null || (quizFinished && timeExpired)) {
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        if (answersGroup.getSelectedToggle() != null) {
            selectedReponses.put(question.getIdQuestion(), (Integer) answersGroup.getSelectedToggle().getUserData());
        } else {
            selectedReponses.remove(question.getIdQuestion());
        }
    }

    private boolean hasSelectionForCurrentQuestion() {
        return answersGroup != null && answersGroup.getSelectedToggle() != null;
    }

    private int countUnansweredQuestions() {
        int unanswered = 0;
        for (Question question : questions) {
            if (!selectedReponses.containsKey(question.getIdQuestion())) {
                unanswered++;
            }
        }
        return unanswered;
    }

    private void startTimer(int dureeMinutes) {
        stopTimer();
        if (dureeMinutes <= 0) {
            timerLabel.setText("Temps libre");
            updateTimerStyle();
            return;
        }

        remainingSeconds = dureeMinutes * 60;
        updateTimerLabel();
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (quizFinished) {
                stopTimer();
                return;
            }
            remainingSeconds = Math.max(remainingSeconds - 1, 0);
            updateTimerLabel();
            if (remainingSeconds == 0) {
                expireQuiz();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void expireQuiz() {
        if (quizFinished) {
            return;
        }
        quizFinished = true;
        timeExpired = true;
        saveCurrentSelection();
        lockQuizInteractions();
        stopTimer();
        submitQuiz(new javafx.event.ActionEvent(answersBox, answersBox), true);
    }

    private void lockQuizInteractions() {
        prevButton.setDisable(true);
        nextButton.setDisable(true);
        submitButton.setDisable(true);
        answersBox.setDisable(true);
        answersBox.setMouseTransparent(true);
        timerLabel.setText("Temps restant : 00:00");
        updateTimerStyle();
    }

    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("Temps restant : %02d:%02d", minutes, seconds));
        updateTimerStyle();
    }

    private void updateTimerStyle() {
        if (timerLabel == null) {
            return;
        }

        timerLabel.getStyleClass().removeAll("session-timer-warning", "session-timer-danger");
        if (remainingSeconds <= 60 && remainingSeconds > 0) {
            timerLabel.getStyleClass().add("session-timer-danger");
            return;
        }
        if (remainingSeconds <= 300 && remainingSeconds > 0) {
            timerLabel.getStyleClass().add("session-timer-warning");
        }
    }

    private void stopTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void switchScene(javafx.event.ActionEvent event, Parent root) {
        Node source = event.getSource() instanceof Node ? (Node) event.getSource() : answersBox;
        Stage stage = (Stage) source.getScene().getWindow();
        ControllerUtils.openInApplication(stage, root, "Resultat Quiz");
    }

    private String safeValue(String value) {
        return value == null ? "-" : value;
    }

    private void translateCurrentQuestionAndAnswers(Button trigger, boolean english) {
        if (questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            ControllerUtils.showWarning("Aucune question a traduire pour le moment.");
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        List<Reponse> responses = responsesByQuestion.getOrDefault(question.getIdQuestion(), List.of());
        List<String> sourceTexts = new ArrayList<>();
        sourceTexts.add(question.getQuestion());
        for (Reponse reponse : responses) {
            sourceTexts.add(reponse.getReponse());
        }

        if (english) {
            translationUiSupport.translateAllToEnglish(trigger, sourceTexts, this::applyTranslatedQuestionAndAnswers);
            return;
        }
        translationUiSupport.translateAllToFrench(trigger, sourceTexts, this::applyTranslatedQuestionAndAnswers);
    }

    private void applyTranslatedQuestionAndAnswers(List<String> translatedTexts) {
        if (translatedTexts == null || translatedTexts.isEmpty()) {
            return;
        }

        questionLabel.setText(translatedTexts.get(0));
        int answerCount = Math.min(answersBox.getChildren().size(), translatedTexts.size() - 1);
        for (int i = 0; i < answerCount; i++) {
            Node node = answersBox.getChildren().get(i);
            if (node instanceof RadioButton radioButton) {
                radioButton.setText(translatedTexts.get(i + 1));
            }
        }
    }
}
