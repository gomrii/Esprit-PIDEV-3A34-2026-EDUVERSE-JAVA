package Controllers;

import Entities.Quiz;
import Services.AccessibilityService;
import Services.SpeechToTextService;
import Utils.Session;
import com.elearning.entity.User;
import com.elearning.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.util.Duration;

public final class ControllerUtils {
    private static final Pattern QUIZ_TITLE_ALLOWED_PATTERN = Pattern.compile("[\\p{L}\\p{Nd} _-]*");
    private static final Map<Button, ScaleTransition> ACCESSIBILITY_PULSE = new WeakHashMap<>();

    private ControllerUtils() {
    }

    public static boolean isTextValid(TextInputControl control) {
        return isTextValid(control, 6);
    }

    public static boolean isTextValid(TextInputControl control, int minLength) {
        return control != null
                && control.getText() != null
                && !control.getText().trim().isEmpty()
                && control.getText().trim().length() >= minLength;
    }

    public static boolean isRequiredFilled(TextInputControl control) {
        return control != null
                && control.getText() != null
                && !control.getText().trim().isEmpty();
    }

    public static boolean isQuizTitleValid(TextInputControl control) {
        return getQuizTitleValidationMessage(control) == null;
    }

    public static String getQuizTitleValidationMessage(TextInputControl control) {
        if (!isRequiredFilled(control)) {
            return "Le titre du quiz est obligatoire.";
        }

        String title = control.getText().trim();
        if (title.length() < 2) {
            return "Le titre doit contenir au moins 2 caracteres.";
        }
        if (!QUIZ_TITLE_ALLOWED_PATTERN.matcher(title).matches()) {
            return "Le titre contient des caracteres non autorises.";
        }

        return null;
    }

    public static void applyQuizTitleFormatter(TextInputControl control) {
        if (control == null) {
            return;
        }

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String nextText = change.getControlNewText();
            return QUIZ_TITLE_ALLOWED_PATTERN.matcher(nextText).matches() ? change : null;
        };

        control.setTextFormatter(new TextFormatter<>(filter));
    }

    public static void startSpeechToText(Button button, TextInputControl control) {
        if (button == null || control == null) {
            return;
        }

        button.setDisable(true);
        String previousText = button.getText();
        button.setText("...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return SpeechToTextService.getInstance().transcribe();
            }
        };

        task.setOnSucceeded(event -> {
            button.setDisable(false);
            button.setText(previousText);

            String transcript = task.getValue() == null ? "" : task.getValue().trim();
            if (transcript.isBlank()) {
                showError("La transcription est vide.");
                return;
            }

            String existingValue = control.getText() == null ? "" : control.getText().trim();
            if (existingValue.isEmpty()) {
                control.setText(transcript);
            } else {
                control.setText(existingValue + " " + transcript);
            }
            control.requestFocus();
            control.positionCaret(control.getText().length());
        });

        task.setOnFailed(event -> {
            button.setDisable(false);
            button.setText(previousText);

            Throwable exception = task.getException();
            String message = buildSpeechToTextErrorMessage(exception);
            showError(message);
        });

        Thread thread = new Thread(task, "speech-to-text-task");
        thread.setDaemon(true);
        thread.start();
    }

    public static boolean isInteger(TextInputControl control) {
        if (!isRequiredFilled(control)) {
            return false;
        }
        try {
            Integer.parseInt(control.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(TextInputControl control) {
        if (!isRequiredFilled(control)) {
            return false;
        }
        try {
            Double.parseDouble(control.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void configureAccessibilityButton(Button button) {
        if (button == null) {
            return;
        }

        button.getStyleClass().removeAll("accessibility-enabled", "accessibility-disabled");
        if (!button.getStyleClass().contains("accessibility-toggle")) {
            button.getStyleClass().add("accessibility-toggle");
        }
        boolean enabled = AccessibilityService.getInstance().isAccessibilityEnabled();
        if (enabled) {
            button.setText("Assistant vocal : ON");
            button.getStyleClass().add("accessibility-enabled");
            startAccessibilityPulse(button);
        } else {
            button.setText("Assistant vocal : OFF");
            button.getStyleClass().add("accessibility-disabled");
            stopAccessibilityPulse(button);
        }
    }

    public static boolean isAccessibilityActive() {
        return AccessibilityService.getInstance().isAccessibilityEnabled();
    }

    public static boolean activateAccessibility(Button button) {
        boolean enabled = AccessibilityService.getInstance().activateAccessibility();
        configureAccessibilityButton(button);
        return enabled;
    }

    public static boolean deactivateAccessibility(Button button) {
        boolean disabled = AccessibilityService.getInstance().deactivateAccessibility();
        configureAccessibilityButton(button);
        return disabled;
    }

    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message);
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Erreur", message);
    }

    public static void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Attention", message);
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        AccessibilityService.getInstance().announceMessage(title + ". " + message);
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String buildSpeechToTextErrorMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "La transcription vocale a echoue.";
        }

        String message = exception.getMessage().trim();
        String lowered = message.toLowerCase();

        if (lowered.contains("vosk_model_path") || lowered.contains("modele vosk")) {
            return "Modele vocal introuvable. " + message;
        }
        if (lowered.contains("micro")) {
            return "Probleme de micro. " + message;
        }
        if (lowered.contains("aucune parole detectee") || lowered.contains("aucun texte n'a ete reconnu")) {
            return "Aucune parole exploitable detectee. Attendez le demarrage du micro puis parlez naturellement.";
        }
        if (lowered.contains("script python") || lowered.contains("module python") || lowered.contains("python est introuvable")) {
            return "Probleme de script local. " + message;
        }

        return message;
    }

    public static void navigateTo(Node source, String fxmlPath) {
        navigateTo(source, fxmlPath, "Impossible d'ouvrir la page demandee.");
    }

    public static void navigateTo(Node source, String fxmlPath, String failurePrefix) {
        if (source == null) {
            showError(buildNavigationErrorMessage(failurePrefix, fxmlPath, "source JavaFX introuvable."));
            return;
        }

        try {
            Parent root = loadFxml(fxmlPath);
            if (source.getScene() == null || source.getScene().getWindow() == null) {
                throw new IllegalStateException("Scene ou fenetre JavaFX introuvable.");
            }
            Stage stage = (Stage) source.getScene().getWindow();
            openInApplication(stage, root, resolveViewTitle(fxmlPath));
        } catch (IOException | IllegalStateException e) {
            showError(buildNavigationErrorMessage(failurePrefix, fxmlPath, e.getMessage()));
        }
    }

    public static Parent loadFxml(String fxmlPath) throws IOException {
        URL resource = ControllerUtils.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalStateException("Fichier FXML introuvable dans src/main/resources: " + fxmlPath);
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            return loader.load();
        } catch (IOException e) {
            throw new IOException(buildFxmlLoadFailureMessage(fxmlPath, e), e);
        }
    }

    public static FXMLLoader createLoader(String fxmlPath) {
        URL resource = ControllerUtils.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalStateException("Fichier FXML introuvable dans src/main/resources: " + fxmlPath);
        }
        return new FXMLLoader(resource);
    }

    public static void applySceneRoot(Stage stage, Parent root) {
        openInApplication(stage, root, null);
    }

    public static void openInApplication(Stage stage, Parent root, String title) {
        if (stage == null) {
            throw new IllegalStateException("Fenetre JavaFX introuvable.");
        }

        MainDashboardController dashboard = MainDashboardController.getInstance();
        if (dashboard != null && dashboard.isAttachedToScene(stage.getScene())) {
            Parent preparedRoot = prepareEmbeddedRoot(root);
            dashboard.loadViewFromParent(preparedRoot, title != null ? title : dashboard.getCurrentPageTitle());
            return;
        }

        Scene scene = stage.getScene();
        if (scene == null) {
            Scene newScene = new Scene(root, 1100, 700);
            attachDefaultStylesheet(newScene);
            AccessibilityService.getInstance().install(newScene);
            stage.setScene(newScene);
            return;
        }

        scene.setRoot(root);
        attachDefaultStylesheet(scene);
    }

    private static void attachDefaultStylesheet(Scene scene) {
        if (scene == null) {
            return;
        }

        URL cssResource = ControllerUtils.class.getResource("/style.css");
        if (cssResource == null) {
            throw new IllegalStateException("Feuille CSS introuvable dans src/main/resources: /style.css");
        }

        String css = cssResource.toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    private static String buildNavigationErrorMessage(String failurePrefix, String fxmlPath, String details) {
        String prefix = (failurePrefix == null || failurePrefix.isBlank())
                ? "Impossible d'ouvrir la page demandee."
                : failurePrefix.trim();
        String suffix = (details == null || details.isBlank()) ? "" : " " + details.trim();
        return prefix + " Ressource: " + fxmlPath + "." + suffix;
    }

    private static String buildFxmlLoadFailureMessage(String fxmlPath, Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        String detail = rootCause.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = throwable.getMessage();
        }
        if (detail == null || detail.isBlank()) {
            detail = "Cause non detaillee.";
        }

        return "Echec de chargement du FXML " + fxmlPath
                + ". Verifiez le fx:controller, les imports JavaFX, le CSS, les images et les autres ressources referencees. Detail: "
                + detail;
    }

    public static void decorateButton(Button button, String iconPath, String... styleClasses) {
        if (button == null) {
            return;
        }

        if (styleClasses != null) {
            for (String styleClass : styleClasses) {
                if (styleClass != null && !styleClass.isBlank() && !button.getStyleClass().contains(styleClass)) {
                    button.getStyleClass().add(styleClass);
                }
            }
        }

        if (iconPath != null && !iconPath.isBlank()) {
            SVGPath icon = new SVGPath();
            icon.setContent(iconPath);
            icon.getStyleClass().add("button-icon");
            button.setGraphic(icon);
            button.setContentDisplay(ContentDisplay.LEFT);
            button.setGraphicTextGap(8);
        }

        button.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        button.setWrapText(false);
    }

    public static String getCurrentRole() {
        User user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user != null && user.getRole() != null && !user.getRole().isBlank()) {
            return user.getRole();
        }
        return Session.role != null ? Session.role : User.ROLE_ETUDIANT;
    }

    public static boolean isAdminRole() {
        return User.ROLE_ADMIN.equalsIgnoreCase(getCurrentRole());
    }

    public static boolean isTeacherRole() {
        return User.ROLE_ENSEIGNANT.equalsIgnoreCase(getCurrentRole());
    }

    public static String getRoleBasedQuizFxml() {
        if (isAdminRole()) {
            return "/admin_quiz_list.fxml";
        }
        if (isTeacherRole()) {
            return "/teacher_quiz_list.fxml";
        }
        return "/student_quiz_list.fxml";
    }

    public static String getRoleBasedQuizTitle() {
        if (isAdminRole()) {
            return "Quiz - Administration";
        }
        if (isTeacherRole()) {
            return "Quiz - Enseignant";
        }
        return "Quiz - Etudiant";
    }

    public static String getRoleBasedDashboardFxml() {
        if (isAdminRole()) {
            return "/com/elearning/gui/AdminDashboardView.fxml";
        }
        if (isTeacherRole()) {
            return "/com/elearning/gui/EnseignantDashboardView.fxml";
        }
        return "/com/elearning/gui/EtudiantDashboardView.fxml";
    }

    public static String getRoleBasedDashboardTitle() {
        if (isAdminRole()) {
            return "Dashboard Admin";
        }
        if (isTeacherRole()) {
            return "Dashboard Enseignant";
        }
        return "Dashboard Etudiant";
    }

    public static String buildQuizCreatorKey() {
        User user = SessionManager.getInstance().getUtilisateurConnecte();
        String role = getCurrentRole();
        if (user == null) {
            return role;
        }
        return role + "#" + user.getId();
    }

    public static boolean isQuizOwnedByCurrentUser(Quiz quiz) {
        if (quiz == null || quiz.getCreatedBy() == null || quiz.getCreatedBy().isBlank()) {
            return false;
        }

        String createdBy = quiz.getCreatedBy().trim();
        if (createdBy.equalsIgnoreCase(buildQuizCreatorKey())) {
            return true;
        }

        if (isTeacherRole()) {
            return createdBy.equalsIgnoreCase("teacher") || createdBy.equalsIgnoreCase("enseignant");
        }
        if (isAdminRole()) {
            return createdBy.equalsIgnoreCase("admin");
        }
        return false;
    }

    public static boolean isAdminOwnedQuiz(Quiz quiz) {
        if (quiz == null || quiz.getCreatedBy() == null) {
            return false;
        }
        String createdBy = quiz.getCreatedBy().trim();
        return createdBy.equalsIgnoreCase("admin") || createdBy.toUpperCase().startsWith(User.ROLE_ADMIN + "#");
    }

    public static String formatQuizCreator(String createdBy) {
        if (createdBy == null || createdBy.isBlank()) {
            return "-";
        }
        String normalized = createdBy.trim();
        if (normalized.equalsIgnoreCase("admin") || normalized.toUpperCase().startsWith(User.ROLE_ADMIN + "#")) {
            return "Admin";
        }
        if (normalized.equalsIgnoreCase("teacher")
                || normalized.equalsIgnoreCase("enseignant")
                || normalized.toUpperCase().startsWith(User.ROLE_ENSEIGNANT + "#")) {
            return "Enseignant";
        }
        return normalized;
    }

    public static String normalizeQuizStatus(String status) {
        if (status == null) {
            return "";
        }

        String normalized = status.trim().toLowerCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "enattente", "en_attente" -> "en_attente";
            case "valide", "valid" -> "valide";
            case "rejete", "rejected" -> "rejete";
            default -> normalized;
        };
    }

    public static String formatQuizStatus(String status) {
        return switch (normalizeQuizStatus(status)) {
            case "valide" -> "Valide";
            case "en_attente" -> "En attente";
            case "rejete" -> "Rejete";
            case "" -> "Sans statut";
            default -> status == null ? "Sans statut" : status.trim();
        };
    }

    public static boolean isValidatedQuizStatus(String status) {
        return "valide".equals(normalizeQuizStatus(status));
    }

    public static boolean isPendingQuizStatus(String status) {
        return "en_attente".equals(normalizeQuizStatus(status));
    }

    public static boolean isRejectedQuizStatus(String status) {
        return "rejete".equals(normalizeQuizStatus(status));
    }

    public static String formatQuizLevel(String level) {
        if (level == null || level.isBlank()) {
            return "-";
        }

        return switch (level.trim().toLowerCase()) {
            case "facile" -> "Facile";
            case "moyen" -> "Moyen";
            case "difficile" -> "Difficile";
            default -> level.trim();
        };
    }

    private static Parent prepareEmbeddedRoot(Parent root) {
        if (root instanceof BorderPane borderPane) {
            borderPane.setLeft(null);
        }
        return root;
    }

    private static String resolveViewTitle(String fxmlPath) {
        if (fxmlPath == null) {
            return "EduVerse";
        }

        return switch (fxmlPath) {
            case "/com/elearning/gui/AdminDashboardView.fxml" -> "Dashboard Admin";
            case "/com/elearning/gui/EnseignantDashboardView.fxml" -> "Dashboard Enseignant";
            case "/com/elearning/gui/EtudiantDashboardView.fxml" -> "Dashboard Etudiant";
            case "/admin_quiz_list.fxml" -> "Quiz - Administration";
            case "/teacher_quiz_list.fxml" -> "Quiz - Enseignant";
            case "/student_quiz_list.fxml" -> "Quiz - Etudiant";
            case "/quiz_add.fxml", "/quiz_add_manual.fxml" -> "Ajout Quiz";
            case "/quiz_edit.fxml" -> "Modification Quiz";
            case "/admin_question_list.fxml", "/teacher_question_list.fxml" -> "Gestion Questions";
            case "/question_add.fxml", "/teacher_question_add.fxml" -> "Ajout Question";
            case "/question_edit.fxml", "/teacher_question_edit.fxml" -> "Modification Question";
            case "/admin_answer_list.fxml", "/teacher_answer_list.fxml" -> "Gestion Reponses";
            case "/answer_add.fxml", "/teacher_answer_add.fxml" -> "Ajout Reponse";
            case "/answer_edit.fxml", "/teacher_answer_edit.fxml" -> "Modification Reponse";
            case "/admin_stats.fxml", "/teacher_stats.fxml", "/student_stats.fxml" -> "Statistiques Quiz";
            case "/student_quiz_pass.fxml" -> "Passage Quiz";
            case "/student_quiz_result.fxml" -> "Resultat Quiz";
            default -> "EduVerse";
        };
    }

    private static void startAccessibilityPulse(Button button) {
        if (button == null) {
            return;
        }

        ScaleTransition transition = ACCESSIBILITY_PULSE.computeIfAbsent(button, key -> {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(1100), key);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.035);
            pulse.setToY(1.035);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            return pulse;
        });

        if (transition.getStatus() != Animation.Status.RUNNING) {
            transition.play();
        }
    }

    private static void stopAccessibilityPulse(Button button) {
        if (button == null) {
            return;
        }

        ScaleTransition transition = ACCESSIBILITY_PULSE.get(button);
        if (transition != null) {
            transition.stop();
        }
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }
}
