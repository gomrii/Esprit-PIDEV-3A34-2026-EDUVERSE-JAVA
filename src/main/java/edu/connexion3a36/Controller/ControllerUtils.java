package edu.connexion3a36.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextFormatter;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class ControllerUtils {
    private static final Pattern QUIZ_TITLE_ALLOWED_PATTERN = Pattern.compile("[\\p{L}\\p{Nd} _-]*");

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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void navigateTo(Node source, String fxmlPath) {
        if (source == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(ControllerUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();
            String css = ControllerUtils.class.getResource("/style.css").toExternalForm();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(css);
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
                if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
            }
        } catch (IOException e) {
            showError("Impossible d'ouvrir la page: " + e.getMessage());
        }
    }
}
