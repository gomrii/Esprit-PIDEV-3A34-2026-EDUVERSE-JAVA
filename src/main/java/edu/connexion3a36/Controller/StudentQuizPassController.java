package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.services.QuestionService;
import edu.connexion3a36.services.ReponseService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button submitButton;

    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
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
            metaLabel.setText("Niveau : " + safeValue(quiz.getLevel()) + " | Durée : " + quiz.getDuree() + " min");
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
                responsesByQuestion.put(question.getIdQuestion(),
                        reponseService.afficherReponsesByIdQuestion(question.getIdQuestion()));
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
            ControllerUtils.showWarning("Veuillez sélectionner une réponse avant de continuer.");
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
                        "Certaines questions ne sont pas encore répondues.\nVoulez-vous vraiment soumettre le quiz ?",
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
            ControllerUtils.showError("Impossible d'afficher le résultat : " + e.getMessage());
        }
    }

    private void renderCurrentQuestion() {
        if (questions.isEmpty()) {
            progressLabel.setText("Aucune question");
            questionLabel.setText("Ce quiz ne contient aucune question.");
            answersBox.getChildren().clear();
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            submitButton.setDisable(false);
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        progressLabel.setText("Question " + (currentQuestionIndex + 1) + " / " + questions.size());
        questionLabel.setText(question.getQuestion());

        answersGroup = new ToggleGroup();
        answersBox.getChildren().clear();
        for (Reponse reponse : responsesByQuestion.getOrDefault(question.getIdQuestion(), List.of())) {
            RadioButton radioButton = new RadioButton(reponse.getReponse());
            radioButton.setUserData(reponse.getIdReponse());
            radioButton.setToggleGroup(answersGroup);
            radioButton.setWrapText(true);
            radioButton.setDisable(quizFinished);
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
        if (questions.isEmpty() || answersGroup == null || quizFinished && timeExpired) {
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
    }

    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("Temps restant : %02d:%02d", minutes, seconds));
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
        Scene scene = stage.getScene();
        if (scene == null) {
            Scene newScene = new Scene(root, 1100, 700);
            newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(newScene);
        } else {
            scene.setRoot(root);
        }
    }

    private String safeValue(String value) {
        return value == null ? "-" : value;
    }
}
