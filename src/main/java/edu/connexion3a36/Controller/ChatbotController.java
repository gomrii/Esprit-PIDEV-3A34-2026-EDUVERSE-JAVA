package edu.connexion3a36.Controller;

import edu.connexion3a36.services.ChatbotService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatbotController {
    @FXML
    private ScrollPane conversationScrollPane;
    @FXML
    private VBox conversationBox;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;

    private final ChatbotService chatbotService = new ChatbotService();
    private final List<ChatbotService.ChatMessage> history = new ArrayList<>();

    @FXML
    public void initialize() {
        addMessage("assistant", "Bonjour. Je reponds aux questions sur les donnees reelles de l application et je peux aussi repondre aux questions generales via l intelligence artificielle.");
        Platform.runLater(() -> {
            if (messageField != null) {
                messageField.requestFocus();
            }
        });
    }

    @FXML
    private void handleSend() {
        if (sendButton != null && sendButton.isDisabled()) {
            return;
        }

        String message = messageField == null ? "" : messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        addMessage("user", message);
        if (messageField != null) {
            messageField.clear();
        }

        List<ChatbotService.ChatMessage> requestHistory = new ArrayList<>(history);
        history.add(new ChatbotService.ChatMessage("user", message));
        setLoadingState(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return chatbotService.sendMessage(requestHistory, message);
            }
        };

        task.setOnSucceeded(event -> {
            String response = task.getValue();
            history.add(new ChatbotService.ChatMessage("assistant", response));
            addMessage("assistant", response);
            setLoadingState(false);
        });

        task.setOnFailed(event -> {
            Throwable throwable = task.getException();
            String errorMessage = buildErrorMessage(throwable);
            history.add(new ChatbotService.ChatMessage("assistant", errorMessage));
            addMessage("system", errorMessage);
            setLoadingState(false);
        });

        Thread thread = new Thread(task, "chatbot-hybrid-request");
        thread.setDaemon(true);
        thread.start();
    }

    private String buildErrorMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isBlank()) {
            return "Une erreur inconnue est survenue lors du traitement de votre demande.";
        }
        return throwable.getMessage().trim();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) conversationBox.getScene().getWindow();
        stage.close();
    }

    private void setLoadingState(boolean loading) {
        if (sendButton != null) {
            sendButton.setDisable(loading);
            sendButton.setText(loading ? "Envoi..." : "Envoyer");
        }
        if (messageField != null) {
            messageField.setDisable(loading);
            if (!loading) {
                messageField.requestFocus();
            }
        }
    }

    private void addMessage(String role, String text) {
        VBox bubble = new VBox(6);
        bubble.getStyleClass().addAll("chat-bubble", bubbleClass(role));

        Label author = new Label(authorLabel(role));
        author.getStyleClass().add("chat-author");

        Label content = new Label(text);
        content.setWrapText(true);
        content.getStyleClass().add("chat-text");

        bubble.getChildren().addAll(author, content);
        bubble.setMaxWidth(320);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10);
        row.getStyleClass().add("chat-row");
        if ("user".equals(role)) {
            row.setAlignment(Pos.CENTER_RIGHT);
            row.getChildren().addAll(spacer, bubble);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(bubble, spacer);
        }

        conversationBox.getChildren().add(row);
        Platform.runLater(() -> conversationScrollPane.setVvalue(1.0));
    }

    private String bubbleClass(String role) {
        return switch (role) {
            case "user" -> "chat-bubble-user";
            case "system" -> "chat-bubble-system";
            default -> "chat-bubble-assistant";
        };
    }

    private String authorLabel(String role) {
        return switch (role) {
            case "user" -> "Vous";
            case "system" -> "Systeme";
            default -> "Assistant EduVerse";
        };
    }

    public static void openDialog(Node source) {
        if (source == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(ChatbotController.class.getResource("/chatbot_dialog.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 430, 560);
            String css = ChatbotController.class.getResource("/style.css").toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }

            Stage dialog = new Stage(StageStyle.DECORATED);
            dialog.setTitle("Assistant EduVerse");
            dialog.initModality(Modality.NONE);
            Window owner = source.getScene() == null ? null : source.getScene().getWindow();
            if (owner != null) {
                dialog.initOwner(owner);
            }
            dialog.setMinWidth(400);
            dialog.setMinHeight(500);
            dialog.setScene(scene);
            dialog.show();
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir Assistant EduVerse : " + e.getMessage());
        }
    }
}
