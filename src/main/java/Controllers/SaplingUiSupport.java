package Controllers;

import Services.SaplingService;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;

import java.util.Locale;

public class SaplingUiSupport {
    private final SaplingService saplingService = new SaplingService();

    public void handleGrammarCheck(Button trigger, TextInputControl control) {
        if (control == null) {
            return;
        }

        runTask(trigger, "Correction...", () -> saplingService.checkGrammar(control.getText()), result -> {
            if (result.hasChanges()) {
                control.setText(result.correctedText());
                control.requestFocus();
                control.positionCaret(control.getText().length());
                ControllerUtils.showInfo("Correction terminee. Le texte a ete mis a jour.");
                return;
            }
            if (result.hasDetectedCorrections()) {
                control.setText(result.correctedText());
                control.requestFocus();
                control.positionCaret(control.getText().length());
                ControllerUtils.showInfo("Correction terminee.");
                return;
            }
            ControllerUtils.showInfo("Aucune correction necessaire.");
        });
    }

    public void handleAiDetection(Button trigger, TextInputControl control) {
        if (control == null) {
            return;
        }

        runTask(trigger, "Analyse...", () -> saplingService.detectAiContent(control.getText()), result -> {
            String message = String.format(
                    Locale.FRANCE,
                    "%s.%nScore IA : %.1f %%",
                    result.verdict(),
                    result.scorePercent()
            );
            ControllerUtils.showInfo(message);
        });
    }

    private <T> void runTask(Button trigger, String loadingText, ThrowingSupplier<T> supplier, java.util.function.Consumer<T> onSuccess) {
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
        });

        task.setOnFailed(event -> {
            trigger.setDisable(initiallyDisabled);
            trigger.setText(initialText);
            Throwable exception = task.getException();
            ControllerUtils.showError(exception == null || exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Une erreur Sapling est survenue."
                    : exception.getMessage());
        });

        Thread thread = new Thread(task, "sapling-ui-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
