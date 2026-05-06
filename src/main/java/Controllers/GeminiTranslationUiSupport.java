package Controllers;

import Services.GeminiTranslationService;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GeminiTranslationUiSupport {
    private final GeminiTranslationService translationService = new GeminiTranslationService();

    public void translateToEnglish(Button trigger, TextInputControl control) {
        if (control == null) {
            return;
        }

        translateToEnglish(trigger, control.getText(), translation -> {
            control.setText(translation);
            control.requestFocus();
            control.positionCaret(control.getText().length());
        });
    }

    public void translateToFrench(Button trigger, TextInputControl control) {
        if (control == null) {
            return;
        }

        translateToFrench(trigger, control.getText(), translation -> {
            control.setText(translation);
            control.requestFocus();
            control.positionCaret(control.getText().length());
        });
    }

    public void translateToEnglish(Button trigger, String text, Consumer<String> onSuccess) {
        runTask(trigger, "Traduction...", () -> translationService.translateToEnglish(text), onSuccess, "Traduction anglaise terminee.");
    }

    public void translateToFrench(Button trigger, String text, Consumer<String> onSuccess) {
        runTask(trigger, "Traduction...", () -> translationService.translateToFrench(text), onSuccess, "Traduction francaise terminee.");
    }

    public void translateAllToEnglish(Button trigger, List<String> texts, Consumer<List<String>> onSuccess) {
        runTask(trigger, "Traduction...", () -> translateAll(texts, true), onSuccess, "Traduction anglaise terminee.");
    }

    public void translateAllToFrench(Button trigger, List<String> texts, Consumer<List<String>> onSuccess) {
        runTask(trigger, "Traduction...", () -> translateAll(texts, false), onSuccess, "Traduction francaise terminee.");
    }

    private List<String> translateAll(List<String> texts, boolean english) throws GeminiTranslationService.GeminiTranslationException {
        List<String> translatedTexts = new ArrayList<>();
        if (texts == null) {
            return translatedTexts;
        }

        for (String text : texts) {
            translatedTexts.add(english
                    ? translationService.translateToEnglish(text)
                    : translationService.translateToFrench(text));
        }
        return translatedTexts;
    }

    private <T> void runTask(Button trigger, String loadingText, ThrowingSupplier<T> supplier, Consumer<T> onSuccess, String successMessage) {
        if (trigger == null) {
            return;
        }

        String initialText = trigger.getText();
        boolean initiallyDisabled = trigger.isDisable();
        trigger.setDisable(true);
        trigger.setText(loadingText);

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return supplier.get();
            }
        };

        task.setOnSucceeded(event -> {
            trigger.setDisable(initiallyDisabled);
            trigger.setText(initialText);
            onSuccess.accept(task.getValue());
            if (successMessage != null && !successMessage.isBlank()) {
                ControllerUtils.showInfo(successMessage);
            }
        });

        task.setOnFailed(event -> {
            trigger.setDisable(initiallyDisabled);
            trigger.setText(initialText);
            Throwable exception = task.getException();
            ControllerUtils.showError(exception == null || exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Une erreur Gemini est survenue."
                    : exception.getMessage());
        });

        Thread thread = new Thread(task, "gemini-translation-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
