package Services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class SpeechToTextService {
    private static final String MODEL_ENV_KEY = "VOSK_MODEL_PATH";
    private static final String SCRIPT_RESOURCE_PATH = "/scripts/speech_to_text_local.py";
    private static final int MAX_ERROR_LINES = 20;

    private static final SpeechToTextService INSTANCE = new SpeechToTextService();

    private final Object workerLock = new Object();
    private final Object workerErrorLock = new Object();

    private Process workerProcess;
    private BufferedWriter workerInput;
    private BufferedReader workerOutput;
    private Thread workerErrorThread;
    private Path extractedScriptPath;
    private Path workerModelPath;

    private final List<String> workerErrorLines = new ArrayList<>();

    private SpeechToTextService() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownWorker, "speech-to-text-shutdown"));
    }

    public static SpeechToTextService getInstance() {
        return INSTANCE;
    }

    public String transcribe() throws IOException, InterruptedException {
        synchronized (workerLock) {
            Path modelPath = resolveModelPath();
            return transcribeWithRetry(modelPath, true);
        }
    }

    private String transcribeWithRetry(Path modelPath, boolean allowRetry) throws IOException, InterruptedException {
        ensureWorkerStarted(modelPath);
        clearWorkerErrors();

        try {
            workerInput.write("TRANSCRIBE");
            workerInput.newLine();
            workerInput.flush();

            String responseLine = workerOutput.readLine();
            if (responseLine == null) {
                throw new WorkerCommunicationException(buildWorkerClosedMessage());
            }

            return parseWorkerResponse(responseLine);
        } catch (WorkerCommunicationException exc) {
            if (allowRetry) {
                restartWorker(modelPath);
                return transcribeWithRetry(modelPath, false);
            }
            throw exc;
        }
    }

    private void ensureWorkerStarted(Path modelPath) throws IOException {
        if (workerProcess != null && workerProcess.isAlive() && modelPath.equals(workerModelPath)) {
            return;
        }

        restartWorker(modelPath);
    }

    private void restartWorker(Path modelPath) throws IOException {
        shutdownWorker();

        Path scriptPath = extractScriptToTempFile();
        List<String> command = buildPythonCommand(scriptPath, true);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(false);
        processBuilder.environment().put(MODEL_ENV_KEY, modelPath.toString());

        Process process = processBuilder.start();
        BufferedWriter input = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
        );
        BufferedReader output = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        );
        BufferedReader error = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8)
        );

        clearWorkerErrors();
        Thread stderrThread = startErrorReader(error);

        String readyLine;
        try {
            readyLine = output.readLine();
        } catch (IOException exc) {
            process.destroyForcibly();
            throw new IOException(buildStartupErrorMessage(), exc);
        }

        if (readyLine == null || !readyLine.startsWith("READY")) {
            process.destroyForcibly();
            throw new IOException(buildStartupErrorMessage());
        }

        workerProcess = process;
        workerInput = input;
        workerOutput = output;
        workerErrorThread = stderrThread;
        workerModelPath = modelPath;
    }

    private Thread startErrorReader(BufferedReader errorReader) {
        Thread thread = new Thread(() -> {
            try (errorReader) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    appendWorkerError(line);
                }
            } catch (IOException exc) {
                appendWorkerError("Erreur de lecture stderr du script Python : " + exc.getMessage());
            }
        }, "speech-to-text-stderr");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private String parseWorkerResponse(String responseLine) throws IOException {
        if (responseLine.startsWith("OK\t")) {
            String transcript = decodePayload(responseLine.substring(3)).trim();
            if (transcript.isBlank()) {
                throw new IOException("La transcription est vide.");
            }
            return transcript;
        }

        if (responseLine.startsWith("ERR\t")) {
            throw new IOException(decodePayload(responseLine.substring(4)));
        }

        throw new WorkerCommunicationException("Reponse invalide du script local de transcription.");
    }

    private String decodePayload(String encodedValue) throws IOException {
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedValue);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exc) {
            throw new IOException("Le script local a retourne une reponse illisible.", exc);
        }
    }

    private Path extractScriptToTempFile() throws IOException {
        if (extractedScriptPath != null && Files.exists(extractedScriptPath)) {
            return extractedScriptPath;
        }

        InputStream inputStream = getClass().getResourceAsStream(SCRIPT_RESOURCE_PATH);
        if (inputStream == null) {
            throw new IOException("Le script Python local de transcription est introuvable dans les ressources.");
        }

        Path tempFile = Files.createTempFile("speech-to-text-", ".py");
        try (inputStream; OutputStream outputStream = Files.newOutputStream(tempFile)) {
            inputStream.transferTo(outputStream);
        }
        tempFile.toFile().deleteOnExit();
        extractedScriptPath = tempFile;
        return tempFile;
    }

    private Path resolveModelPath() throws IOException {
        String rawModelPath = System.getenv(MODEL_ENV_KEY);
        if (rawModelPath == null || rawModelPath.isBlank()) {
            throw new IOException(
                    "Le modele Vosk est introuvable : la variable d'environnement VOSK_MODEL_PATH n'est pas definie."
            );
        }

        String normalizedValue = stripQuotes(rawModelPath.trim());
        final Path modelPath;
        try {
            modelPath = Paths.get(normalizedValue).toAbsolutePath().normalize();
        } catch (InvalidPathException exc) {
            throw new IOException(
                    "Le chemin du modele Vosk defini dans VOSK_MODEL_PATH est invalide : " + normalizedValue,
                    exc
            );
        }

        if (!Files.exists(modelPath)) {
            throw new IOException("Le dossier du modele Vosk est introuvable : " + modelPath);
        }
        if (!Files.isDirectory(modelPath)) {
            throw new IOException("VOSK_MODEL_PATH ne pointe pas vers un dossier valide : " + modelPath);
        }

        return modelPath;
    }

    private List<String> buildPythonCommand(Path scriptPath, boolean workerMode) throws IOException {
        List<List<String>> candidates = new ArrayList<>();
        candidates.add(List.of("python", "-u"));
        candidates.add(List.of("python3", "-u"));
        candidates.add(List.of("py", "-3", "-u"));

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            Path pythonRoot = Paths.get(localAppData, "Programs", "Python");
            if (Files.isDirectory(pythonRoot)) {
                try (var paths = Files.list(pythonRoot)) {
                    paths.filter(Files::isDirectory)
                            .map(path -> path.resolve("python.exe"))
                            .filter(Files::exists)
                            .sorted()
                            .forEach(path -> candidates.add(List.of(path.toString(), "-u")));
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
                    if (workerMode) {
                        command.add("--worker");
                    }
                    return command;
                }
            } catch (IOException | InterruptedException ignored) {
                if (ignored instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new IOException("Python est introuvable. Installez Python 3 puis relancez la saisie vocale.");
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private void appendWorkerError(String line) {
        if (line == null) {
            return;
        }

        synchronized (workerErrorLock) {
            if (workerErrorLines.size() == MAX_ERROR_LINES) {
                workerErrorLines.remove(0);
            }
            workerErrorLines.add(line.trim());
        }
    }

    private void clearWorkerErrors() {
        synchronized (workerErrorLock) {
            workerErrorLines.clear();
        }
    }

    private String snapshotWorkerErrors() {
        synchronized (workerErrorLock) {
            return workerErrorLines.stream()
                    .filter(line -> line != null && !line.isBlank())
                    .reduce((left, right) -> left + System.lineSeparator() + right)
                    .orElse("");
        }
    }

    private String buildStartupErrorMessage() {
        String stderr = snapshotWorkerErrors();
        if (!stderr.isBlank()) {
            return firstNonBlankLine(stderr);
        }
        return "Le script Python local n'a pas pu demarrer.";
    }

    private String buildWorkerClosedMessage() {
        String stderr = snapshotWorkerErrors();
        if (!stderr.isBlank()) {
            return firstNonBlankLine(stderr);
        }
        return "Le script Python local s'est ferme avant de retourner une transcription.";
    }

    private String firstNonBlankLine(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        for (String line : content.split("\\R")) {
            String trimmed = line == null ? "" : line.trim();
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }

        return "";
    }

    private void shutdownWorker() {
        synchronized (workerLock) {
            closeWorkerProcess();
        }
    }

    private void closeWorkerProcess() {
        if (workerInput != null) {
            try {
                workerInput.write("EXIT");
                workerInput.newLine();
                workerInput.flush();
            } catch (IOException ignored) {
            }
        }

        if (workerProcess != null) {
            workerProcess.destroy();
            try {
                workerProcess.waitFor();
            } catch (InterruptedException exc) {
                Thread.currentThread().interrupt();
            }
            if (workerProcess.isAlive()) {
                workerProcess.destroyForcibly();
            }
        }

        workerProcess = null;
        workerInput = null;
        workerOutput = null;
        workerErrorThread = null;
        workerModelPath = null;
        clearWorkerErrors();
    }

    private static final class WorkerCommunicationException extends IOException {
        private WorkerCommunicationException(String message) {
            super(message);
        }
    }
}
