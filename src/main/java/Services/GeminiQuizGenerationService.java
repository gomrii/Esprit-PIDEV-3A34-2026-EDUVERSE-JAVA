package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Entities.Question;
import Entities.Quiz;
import Entities.Reponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GeminiQuizGenerationService {
    private static final String GOOGLE_API_KEY_ENV = "GOOGLE_API_KEY";
    private static final String GEMINI_API_KEY_ENV = "GEMINI_API_KEY";
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String GENERATE_CONTENT_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent";
    private static final String MISSING_API_KEY_MESSAGE =
            "Aucune cle Gemini detectee. Verifiez GOOGLE_API_KEY ou GEMINI_API_KEY, puis relancez IntelliJ et l'application.";
    private static final String INVALID_API_KEY_MESSAGE = "Cle API Gemini invalide.";
    private static final String QUOTA_EXCEEDED_MESSAGE = "Quota Gemini depasse. Reessayez plus tard.";
    private static final String NETWORK_ERROR_MESSAGE = "Impossible de contacter Gemini.";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Le service Gemini est momentanement indisponible.";
    private static final String INVALID_JSON_MESSAGE = "Reponse Gemini invalide.";
    private static final String EMPTY_RESPONSE_MESSAGE = "Reponse Gemini vide.";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Quiz generateQuiz(String titre, int duree, String niveau, int nombreQuestions)
            throws GeminiQuizGenerationException {
        return generateQuiz(titre, duree, niveau, nombreQuestions, List.of());
    }

    public Quiz generateQuiz(String titre, int duree, String niveau, int nombreQuestions, List<String> existingQuestions)
            throws GeminiQuizGenerationException {
        String apiKey = readApiKey();
        String requestBody = buildRequestBody(titre, duree, niveau, nombreQuestions, existingQuestions);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GENERATE_CONTENT_URL))
                .timeout(Duration.ofSeconds(60))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logDebug("Erreur reseau Gemini quiz: " + e.getMessage());
            throw new GeminiQuizGenerationException(NETWORK_ERROR_MESSAGE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeminiQuizGenerationException("La generation Gemini a ete interrompue.", e);
        }

        String responseBody = response.body() == null ? "" : response.body().trim();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw buildHttpException(response.statusCode(), responseBody);
        }
        if (responseBody.isBlank()) {
            logDebug("Gemini quiz a retourne une reponse HTTP vide.");
            throw new GeminiQuizGenerationException(EMPTY_RESPONSE_MESSAGE);
        }

        String generatedJson = extractGeneratedJson(responseBody);
        return parseQuizJson(generatedJson, titre, duree, niveau, nombreQuestions);
    }

    private String readApiKey() throws GeminiQuizGenerationException {
        String googleApiKey = System.getenv(GOOGLE_API_KEY_ENV);
        if (googleApiKey != null && !googleApiKey.isBlank()) {
            System.out.println("DEBUG: GOOGLE_API_KEY detectee");
            return googleApiKey.trim();
        }

        String geminiApiKey = System.getenv(GEMINI_API_KEY_ENV);
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            System.out.println("DEBUG: GEMINI_API_KEY detectee");
            return geminiApiKey.trim();
        }

        System.out.println("DEBUG: aucune cle Gemini trouvee");
        throw new GeminiQuizGenerationException(MISSING_API_KEY_MESSAGE);
    }

    private String buildRequestBody(String titre, int duree, String niveau, int nombreQuestions, List<String> existingQuestions)
            throws GeminiQuizGenerationException {
        String duplicateAvoidance = buildDuplicateAvoidanceBlock(existingQuestions);
        String prompt = """
                Genere un quiz au format JSON strict sur le theme suivant : %s.
                Niveau : %s.
                Duree : %d minutes.
                Nombre de questions : %d.
                Retourne uniquement un JSON valide.
                Le JSON doit respecter exactement cette structure :
                {
                  "title": "Quiz Java",
                  "level": "moyen",
                  "duration": 10,
                  "questions": [
                    {
                      "question": "Qu'est-ce qu'une classe en Java ?",
                      "options": [
                        "Un fichier image",
                        "Un modele pour creer des objets",
                        "Une base de donnees",
                        "Un navigateur"
                      ],
                      "answer": 1
                    }
                  ]
                }
                Regles obligatoires :
                - aucun texte avant ou apres le JSON
                - toujours un JSON valide
                - toujours exactement 4 options par question
                - answer = index entier de la bonne reponse entre 0 et 3
                - le nombre de questions doit etre exactement %d
                - le contenu doit etre pedagogique, coherent et directement exploitable
                %s
                """.formatted(titre, niveau, duree, nombreQuestions, nombreQuestions, duplicateAvoidance);

        try {
            return objectMapper.writeValueAsString(new GenerateContentRequest(
                    new Content[]{
                            new Content("user", new Part[]{new Part(prompt)})
                    },
                    new GenerationConfig(0.5d, "application/json")
            ));
        } catch (IOException e) {
            throw new GeminiQuizGenerationException("Impossible de preparer la requete Gemini.", e);
        }
    }

    private String extractGeneratedJson(String responseBody) throws GeminiQuizGenerationException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                logDebug("Aucun candidat Gemini pour quiz. Payload: " + responseBody);
                throw new GeminiQuizGenerationException(EMPTY_RESPONSE_MESSAGE);
            }

            StringBuilder builder = new StringBuilder();
            for (JsonNode part : candidates.path(0).path("content").path("parts")) {
                String text = part.path("text").asText("");
                if (!text.isBlank()) {
                    if (builder.length() > 0) {
                        builder.append(System.lineSeparator());
                    }
                    builder.append(text.trim());
                }
            }

            String generatedText = builder.toString().trim();
            if (generatedText.isBlank()) {
                logDebug("Texte Gemini vide pour quiz. Payload: " + responseBody);
                throw new GeminiQuizGenerationException(EMPTY_RESPONSE_MESSAGE);
            }

            return stripMarkdownFence(generatedText);
        } catch (IOException e) {
            throw new GeminiQuizGenerationException(INVALID_JSON_MESSAGE, e);
        }
    }

    private Quiz parseQuizJson(String generatedJson, String fallbackTitle, int fallbackDuration, String fallbackLevel,
                               int expectedQuestionCount) throws GeminiQuizGenerationException {
        try {
            JsonNode root = objectMapper.readTree(generatedJson);
            JsonNode questionsNode = root.path("questions");
            if (!questionsNode.isArray() || questionsNode.isEmpty()) {
                throw new GeminiQuizGenerationException(INVALID_JSON_MESSAGE);
            }
            if (questionsNode.size() != expectedQuestionCount) {
                throw new GeminiQuizGenerationException(INVALID_JSON_MESSAGE);
            }

            Quiz quiz = new Quiz();
            quiz.setTitre(readNonBlankText(root, "title", fallbackTitle));
            quiz.setLevel(readNonBlankText(root, "level", fallbackLevel));
            quiz.setDuree(root.path("duration").isInt() ? root.path("duration").asInt() : fallbackDuration);

            List<Question> questions = new ArrayList<>();
            for (JsonNode questionNode : questionsNode) {
                String questionText = questionNode.path("question").asText("").trim();
                JsonNode optionsNode = questionNode.path("options");
                int answerIndex = questionNode.path("answer").asInt(-1);

                if (questionText.isBlank() || !optionsNode.isArray() || optionsNode.size() != 4 || answerIndex < 0 || answerIndex > 3) {
                    throw new GeminiQuizGenerationException(INVALID_JSON_MESSAGE);
                }

                Question question = new Question();
                question.setQuestion(questionText);

                List<Reponse> reponses = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    String optionText = optionsNode.path(i).asText("").trim();
                    if (optionText.isBlank()) {
                        throw new GeminiQuizGenerationException(INVALID_JSON_MESSAGE);
                    }
                    Reponse reponse = new Reponse();
                    reponse.setReponse(optionText);
                    reponse.setScore(i == answerIndex ? 1.0 : 0.0);
                    reponses.add(reponse);
                }

                question.setReponses(reponses);
                questions.add(question);
            }

            quiz.setQuestions(questions);
            return quiz;
        } catch (IOException e) {
            throw new GeminiQuizGenerationException(INVALID_JSON_MESSAGE, e);
        }
    }

    private String readNonBlankText(JsonNode root, String fieldName, String fallbackValue) {
        String value = root.path(fieldName).asText("").trim();
        return value.isBlank() ? fallbackValue : value;
    }

    private GeminiQuizGenerationException buildHttpException(int statusCode, String responseBody) {
        String apiMessage = extractApiMessage(responseBody);
        logDebug("Erreur HTTP Gemini quiz " + statusCode + " - message brut: "
                + (apiMessage.isBlank() ? "<aucun message>" : apiMessage));

        if (statusCode == 401 || statusCode == 403) {
            return new GeminiQuizGenerationException(INVALID_API_KEY_MESSAGE);
        }
        if (statusCode == 429) {
            return new GeminiQuizGenerationException(QUOTA_EXCEEDED_MESSAGE);
        }
        if (statusCode >= 500) {
            return new GeminiQuizGenerationException(SERVICE_UNAVAILABLE_MESSAGE);
        }
        return new GeminiQuizGenerationException("Erreur HTTP Gemini.");
    }

    private String extractApiMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody == null ? "" : responseBody);
            JsonNode error = root.path("error");
            if (error.hasNonNull("message")) {
                return error.path("message").asText("");
            }
            if (root.hasNonNull("message")) {
                return root.path("message").asText("");
            }
        } catch (IOException ignored) {
        }
        return "";
    }

    private String stripMarkdownFence(String text) {
        String normalized = text.trim();
        if (normalized.startsWith("```")) {
            int firstLineEnd = normalized.indexOf('\n');
            if (firstLineEnd >= 0) {
                normalized = normalized.substring(firstLineEnd + 1).trim();
            }
            if (normalized.endsWith("```")) {
                normalized = normalized.substring(0, normalized.length() - 3).trim();
            }
        }
        return normalized;
    }

    private String buildDuplicateAvoidanceBlock(List<String> existingQuestions) {
        if (existingQuestions == null || existingQuestions.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Questions deja presentes dans la base pour ce theme, a ne surtout pas reutiliser telles quelles :");
        for (String question : existingQuestions) {
            if (question != null && !question.isBlank()) {
                builder.append(System.lineSeparator()).append("- ").append(question.trim());
            }
        }
        return builder.toString();
    }

    private void logDebug(String message) {
        System.out.println("DEBUG: " + message);
    }

    public static class GeminiQuizGenerationException extends Exception {
        public GeminiQuizGenerationException(String message) {
            super(message);
        }

        public GeminiQuizGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record GenerateContentRequest(Content[] contents, GenerationConfig generationConfig) {
    }

    private record Content(String role, Part[] parts) {
    }

    private record Part(String text) {
    }

    private record GenerationConfig(double temperature, String responseMimeType) {
    }
}
