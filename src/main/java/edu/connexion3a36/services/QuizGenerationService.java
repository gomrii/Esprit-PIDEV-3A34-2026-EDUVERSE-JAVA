package edu.connexion3a36.services;

import edu.connexion3a36.entities.Question;
import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.entities.Reponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QuizGenerationService {

    public Quiz generateQuiz(String titre, int duree, String niveau, int nombreQuestions) throws IOException, InterruptedException {
        Path scriptPath = extractScriptToTempFile();
        List<String> command = buildPythonCommand(scriptPath, titre, String.valueOf(duree), niveau, String.valueOf(nombreQuestions));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(false);
        Process process = processBuilder.start();

        String stdout = read(process.getInputStream());
        String stderr = read(process.getErrorStream());
        int exitCode = process.waitFor();

        try {
            Files.deleteIfExists(scriptPath);
        } catch (IOException ignored) {
        }

        if (exitCode != 0) {
            throw new IOException(buildErrorMessage(stderr));
        }

        return parseOutput(stdout, titre, duree, niveau);
    }

    private List<String> buildPythonCommand(Path scriptPath, String titre, String duree, String niveau, String nombreQuestions) throws IOException {
        List<List<String>> candidates = new ArrayList<>();
        candidates.add(List.of("python"));
        candidates.add(List.of("python3"));
        candidates.add(List.of("py", "-3"));

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            Path standardPython = Paths.get(localAppData, "Programs", "Python", "Python314", "python.exe");
            if (Files.exists(standardPython)) {
                candidates.add(List.of(standardPython.toString()));
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
                    command.add(titre);
                    command.add(duree);
                    command.add(niveau);
                    command.add(nombreQuestions);
                    return command;
                }
            } catch (IOException | InterruptedException ignored) {
            }
        }
        throw new IOException("Python est introuvable. Installez Python 3 puis relancez la generation IA.");
    }

    private Quiz parseOutput(String output, String titre, int duree, String niveau) throws IOException {
        Quiz quiz = new Quiz();
        quiz.setTitre(titre);
        quiz.setDuree(duree);
        quiz.setLevel(niveau);

        List<Question> questions = new ArrayList<>();
        Question currentQuestion = null;

        for (String rawLine : output.split("\\R")) {
            String line = rawLine == null ? "" : rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("QUESTION|")) {
                if (currentQuestion != null) {
                    questions.add(currentQuestion);
                }
                currentQuestion = new Question();
                currentQuestion.setQuestion(decode(line.substring("QUESTION|".length())));
            } else if (line.startsWith("ANSWER|")) {
                if (currentQuestion == null) {
                    throw new IOException("Sortie du script invalide: reponse sans question.");
                }
                String[] parts = line.split("\\|", 3);
                if (parts.length < 3) {
                    throw new IOException("Sortie du script invalide: reponse incomplete.");
                }
                Reponse reponse = new Reponse();
                reponse.setReponse(decode(parts[1]));
                reponse.setScore(parseScore(parts[2]));
                currentQuestion.addReponse(reponse);
            } else if (line.equals("ENDQUESTION")) {
                if (currentQuestion != null) {
                    questions.add(currentQuestion);
                    currentQuestion = null;
                }
            }
        }

        if (currentQuestion != null) {
            questions.add(currentQuestion);
        }

        if (questions.isEmpty()) {
            throw new IOException("Le script Python n a retourne aucune question.");
        }

        quiz.setQuestions(questions);
        return quiz;
    }

    private double parseScore(String scoreValue) throws IOException {
        try {
            return Double.parseDouble(scoreValue);
        } catch (NumberFormatException e) {
            throw new IOException("Score invalide renvoye par le script Python.");
        }
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private Path extractScriptToTempFile() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/scripts/quiz_generator.py");
        if (inputStream == null) {
            throw new IOException("Le script Python de generation est introuvable dans les ressources.");
        }

        Path tempFile = Files.createTempFile("quiz-generator-", ".py");
        try (inputStream; OutputStream outputStream = Files.newOutputStream(tempFile)) {
            inputStream.transferTo(outputStream);
        }
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    private String read(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private String buildErrorMessage(String stderr) {
        if (stderr == null || stderr.isBlank()) {
            return "La generation IA a echoue sans message d erreur.";
        }
        return "La generation IA a echoue: " + stderr.trim();
    }
}
