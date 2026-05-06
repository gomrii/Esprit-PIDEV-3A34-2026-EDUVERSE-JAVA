package Services;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class AccessibilityService {

    private static final String SCENE_INSTALLED_KEY = "accessibility.scene.installed";
    private static final String NODE_WIRED_KEY = "accessibility.node.wired";
    private static final String CHILDREN_WIRED_KEY = "accessibility.children.wired";
    private static final AccessibilityService INSTANCE = new AccessibilityService();

    private final ThreadPoolExecutor speechExecutor = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            runnable -> {
                Thread thread = new Thread(runnable, "accessibility-speech");
                thread.setDaemon(true);
                return thread;
            }
    );

    private volatile String lastMessage = "";
    private volatile long lastMessageAt = 0L;
    private volatile long suppressAnnouncementsUntil = 0L;
    private volatile Path speechScriptPath;
    private volatile boolean accessibilityEnabled = false;
    private volatile Process currentSpeechProcess;

    private AccessibilityService() {
    }

    public static AccessibilityService getInstance() {
        return INSTANCE;
    }

    public void install(Scene scene) {
        if (scene == null || Boolean.TRUE.equals(scene.getProperties().get(SCENE_INSTALLED_KEY))) {
            return;
        }

        scene.getProperties().put(SCENE_INSTALLED_KEY, Boolean.TRUE);
        scene.rootProperty().addListener((obs, oldRoot, newRoot) -> {
            if (newRoot != null) {
                Platform.runLater(() -> {
                    wireNodeTree(newRoot);
                    announcePage(newRoot);
                });
            }
        });
        scene.focusOwnerProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Platform.runLater(() -> announceFocusedNode(newNode));
            }
        });
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F8) {
                announcePage(scene.getRoot());
                event.consume();
            }
        });

        if (scene.getRoot() != null) {
            Platform.runLater(() -> wireNodeTree(scene.getRoot()));
        }
    }

    public boolean isAccessibilityEnabled() {
        return accessibilityEnabled;
    }

    public synchronized boolean activateAccessibility() {
        if (accessibilityEnabled) {
            return false;
        }

        accessibilityEnabled = true;
        suppressAnnouncementsUntil = System.currentTimeMillis() + 800;
        clearSpeechQueue();
        stopCurrentSpeech();
        lastMessage = "";
        lastMessageAt = 0L;
        speakInternal("Mode accessibilite active.", true);
        return true;
    }

    public synchronized boolean deactivateAccessibility() {
        if (!accessibilityEnabled) {
            return false;
        }

        accessibilityEnabled = false;
        suppressAnnouncementsUntil = 0L;
        clearSpeechQueue();
        stopCurrentSpeech();
        lastMessage = "";
        lastMessageAt = 0L;
        return true;
    }

    public void announcePage(Node root) {
        if (!shouldAnnounce()) {
            return;
        }
        String message = buildPageAnnouncement(root);
        if (!message.isBlank()) {
            speakInternal(message, false);
        }
    }

    public void announceMessage(String message) {
        if (!shouldAnnounce()) {
            return;
        }
        if (message != null && !message.isBlank()) {
            speakInternal(message, false);
        }
    }

    private void announceFocusedNode(Node node) {
        if (!shouldAnnounce()) {
            return;
        }
        String description = describeNode(node, false);
        if (!description.isBlank()) {
            speakInternal(description, false);
        }
    }

    private boolean shouldAnnounce() {
        return accessibilityEnabled && System.currentTimeMillis() >= suppressAnnouncementsUntil;
    }

    private void wireNodeTree(Node node) {
        if (node == null) {
            return;
        }

        wireNode(node);
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(this::wireNodeTree);
            if (!Boolean.TRUE.equals(parent.getProperties().get(CHILDREN_WIRED_KEY))) {
                parent.getProperties().put(CHILDREN_WIRED_KEY, Boolean.TRUE);
                parent.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            for (Node added : change.getAddedSubList()) {
                                Platform.runLater(() -> wireNodeTree(added));
                            }
                        }
                    }
                });
            }
        }
    }

    private void wireNode(Node node) {
        if (Boolean.TRUE.equals(node.getProperties().get(NODE_WIRED_KEY))) {
            return;
        }

        node.getProperties().put(NODE_WIRED_KEY, Boolean.TRUE);
        node.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            String description = describeNode(node, false);
            if (!description.isBlank()) {
                speakInternal(description, false);
            }
        });
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            String description = describeNode(node, true);
            if (!description.isBlank()) {
                speakInternal(description, false);
            }
        });
    }

    private String buildPageAnnouncement(Node root) {
        if (root == null) {
            return "";
        }

        String title = findPageTitle(root);
        Set<String> buttons = new LinkedHashSet<>();
        Set<String> fields = new LinkedHashSet<>();
        Set<String> importantInfos = new LinkedHashSet<>();

        collectNodeData(root, buttons, fields, importantInfos);

        List<String> parts = new ArrayList<>();
        if (!title.isBlank()) {
            parts.add("Vous etes dans " + title + ".");
        }
        if (!buttons.isEmpty()) {
            parts.add("Boutons disponibles : " + joinLimited(buttons, 8) + ".");
        }
        if (!fields.isEmpty()) {
            parts.add("Champs disponibles : " + joinLimited(fields, 6) + ".");
        }
        if (!importantInfos.isEmpty()) {
            parts.add("Informations : " + joinLimited(importantInfos, 4) + ".");
        }
        parts.add("Appuyez sur F8 pour relire la page.");

        return String.join(" ", parts).trim();
    }

    private void collectNodeData(Node node, Set<String> buttons, Set<String> fields, Set<String> infos) {
        if (node == null || !node.isVisible()) {
            return;
        }

        if (node instanceof ButtonBase button) {
            addIfPresent(buttons, textOf(button));
        } else if (node instanceof TextInputControl input) {
            addIfPresent(fields, readableInputName(input));
        } else if (node instanceof ChoiceBox<?>) {
            addIfPresent(fields, "liste deroulante");
        } else if (node instanceof ComboBoxBase<?>) {
            addIfPresent(fields, "selection");
        } else if (node instanceof Label label) {
            String text = textOf(label);
            if (hasAnyStyleClass(label, "list-summary", "page-subtitle", "sidebar-username", "field-label")) {
                addIfPresent(infos, text);
            }
        } else if (isCardNode(node)) {
            addIfPresent(infos, cardSummary(node));
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectNodeData(child, buttons, fields, infos);
            }
        }
    }

    private String findPageTitle(Node root) {
        List<Label> labels = new ArrayList<>();
        collectLabels(root, labels);
        for (Label label : labels) {
            if (hasAnyStyleClass(label, "page-title", "form-title")) {
                return sanitize(label.getText());
            }
        }
        for (Label label : labels) {
            String text = sanitize(label.getText());
            if (!text.isBlank()) {
                return text;
            }
        }
        return "la page en cours";
    }

    private void collectLabels(Node node, List<Label> labels) {
        if (node == null || !node.isVisible()) {
            return;
        }
        if (node instanceof Label label) {
            labels.add(label);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectLabels(child, labels);
            }
        }
    }

    private String describeNode(Node node, boolean clicked) {
        if (!shouldAnnounce() || node == null || !node.isVisible() || hasAnyStyleClass(node, "accessibility-toggle")) {
            return "";
        }

        String description;
        if (node instanceof ButtonBase button) {
            description = prefix(clicked, "Bouton", textOf(button));
        } else if (node instanceof TextInputControl input) {
            description = prefix(clicked, "Champ", readableInputName(input));
        } else if (node instanceof ChoiceBox<?>) {
            description = clicked ? "Liste deroulante selectionnee." : "Liste deroulante.";
        } else if (node instanceof ComboBoxBase<?>) {
            description = clicked ? "Selection ouverte." : "Selection.";
        } else if (node instanceof Labeled labeled && hasAnyStyleClass(node, "sidebar-item", "page-title", "field-label")) {
            description = clicked ? sanitize(labeled.getText()) + " selectionne." : sanitize(labeled.getText()) + ".";
        } else if (isCardNode(node)) {
            String summary = cardSummary(node);
            description = summary.isBlank() ? "" : (clicked ? "Carte " + summary + " selectionnee." : "Carte " + summary + ".");
        } else {
            description = "";
        }

        return sanitize(description);
    }

    private String prefix(boolean clicked, String type, String text) {
        String normalized = sanitize(text);
        if (normalized.isBlank()) {
            return "";
        }
        return clicked ? type + " " + normalized + " selectionne." : type + " " + normalized + ".";
    }

    private String readableInputName(TextInputControl input) {
        String text = sanitize(input.getPromptText());
        if (text.isBlank()) {
            text = sanitize(input.getAccessibleText());
        }
        if (text.isBlank() && input.getId() != null) {
            text = sanitize(input.getId().replace("TF", "").replace("TA", ""));
        }
        return text.isBlank() ? "saisie" : text;
    }

    private boolean isCardNode(Node node) {
        return hasAnyStyleClass(node, "dashboard-card", "home-card", "stat-card");
    }

    private String cardSummary(Node node) {
        Set<String> texts = new LinkedHashSet<>();
        collectTexts(node, texts);
        return joinLimited(texts, 4);
    }

    private void collectTexts(Node node, Set<String> texts) {
        if (node == null || !node.isVisible()) {
            return;
        }
        if (node instanceof Labeled labeled) {
            addIfPresent(texts, textOf(labeled));
        } else if (node instanceof TextInputControl input) {
            addIfPresent(texts, readableInputName(input));
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectTexts(child, texts);
            }
        }
    }

    private boolean hasAnyStyleClass(Node node, String... styleClasses) {
        if (node == null) {
            return false;
        }
        for (String styleClass : styleClasses) {
            if (node.getStyleClass().contains(styleClass)) {
                return true;
            }
        }
        return false;
    }

    private void addIfPresent(Collection<String> target, String text) {
        String normalized = sanitize(text);
        if (!normalized.isBlank()) {
            target.add(normalized);
        }
    }

    private String textOf(Labeled labeled) {
        if (labeled == null) {
            return "";
        }
        return sanitize(labeled.getText());
    }

    private String joinLimited(Collection<String> values, int maxItems) {
        List<String> items = new ArrayList<>();
        for (String value : values) {
            String normalized = sanitize(value);
            if (!normalized.isBlank()) {
                items.add(normalized);
            }
            if (items.size() == maxItems) {
                break;
            }
        }
        return String.join(", ", items);
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void speakInternal(String message, boolean force) {
        if (!force && !shouldAnnounce()) {
            return;
        }

        String normalized = sanitize(message);
        if (normalized.isBlank()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (normalized.equals(lastMessage) && now - lastMessageAt < 1500) {
            return;
        }
        lastMessage = normalized;
        lastMessageAt = now;

        speechExecutor.submit(() -> {
            try {
                Path script = ensureSpeechScript();
                List<String> command = buildPythonCommand(script);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();
                currentSpeechProcess = process;
                try (OutputStream outputStream = process.getOutputStream()) {
                    outputStream.write(normalized.getBytes(StandardCharsets.UTF_8));
                }
                process.waitFor();
                readSilently(process.getInputStream());
                readSilently(process.getErrorStream());
            } catch (IOException | InterruptedException ignored) {
                if (ignored instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                currentSpeechProcess = null;
            }
        });
    }

    private void clearSpeechQueue() {
        speechExecutor.getQueue().clear();
    }

    private void stopCurrentSpeech() {
        Process process = currentSpeechProcess;
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private Path ensureSpeechScript() throws IOException {
        if (speechScriptPath != null && Files.exists(speechScriptPath)) {
            return speechScriptPath;
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/scripts/accessibility_speech.py")) {
            if (inputStream == null) {
                throw new IOException("Script de synthese vocale introuvable.");
            }

            Path tempFile = Files.createTempFile("accessibility-speech-", ".py");
            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                inputStream.transferTo(outputStream);
            }
            tempFile.toFile().deleteOnExit();
            speechScriptPath = tempFile;
            return tempFile;
        }
    }

    private List<String> buildPythonCommand(Path scriptPath) throws IOException {
        List<List<String>> candidates = new ArrayList<>();
        candidates.add(List.of("python"));
        candidates.add(List.of("python3"));
        candidates.add(List.of("py", "-3"));

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            Path pythonRoot = Paths.get(localAppData, "Programs", "Python");
            if (Files.isDirectory(pythonRoot)) {
                try (var paths = Files.list(pythonRoot)) {
                    paths.filter(Files::isDirectory)
                            .map(path -> path.resolve("python.exe"))
                            .filter(Files::exists)
                            .sorted()
                            .forEach(path -> candidates.add(List.of(path.toString())));
                }
            }
        }

        for (List<String> candidate : candidates) {
            try {
                List<String> versionCommand = new ArrayList<>(candidate);
                versionCommand.add("--version");
                Process versionProcess = new ProcessBuilder(versionCommand).start();
                int exitCode = versionProcess.waitFor();
                if (exitCode == 0) {
                    List<String> command = new ArrayList<>(candidate);
                    command.add(scriptPath.toString());
                    return command;
                }
            } catch (IOException | InterruptedException ignored) {
                if (ignored instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new IOException("Python est introuvable pour la synthese vocale.");
    }

    private void readSilently(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            while (reader.readLine() != null) {
                // discard output
            }
        } catch (IOException ignored) {
        }
    }
}
