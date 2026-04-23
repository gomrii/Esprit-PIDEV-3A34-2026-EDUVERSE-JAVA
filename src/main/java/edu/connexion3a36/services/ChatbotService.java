package edu.connexion3a36.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatbotService {
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL_NAME = "llama-3.3-70b-versatile";
    private static final String SYSTEM_PROMPT =
            "Tu es Assistant EduVerse, un assistant clair et utile pour une plateforme de gestion de quiz, questions, reponses et statistiques. "
                    + "Quand la question n est pas une question SQL interne deja geree par l application, reponds en francais de facon concise, utile et naturelle.";

    private final ChatbotDatabaseService chatbotDatabaseService = new ChatbotDatabaseService();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public String sendMessage(List<ChatMessage> history, String userMessage) throws ChatbotException {
        String normalizedMessage = normalize(userMessage);
        if (normalizedMessage.isBlank()) {
            throw new ChatbotException("Le message a envoyer est vide.");
        }

        try {
            String databaseResponse = tryHandleDatabaseQuestion(normalizedMessage);
            if (databaseResponse != null) {
                return databaseResponse;
            }
        } catch (SQLException e) {
            throw new ChatbotException("Impossible d interroger la base de donnees : " + e.getMessage(), e);
        }

        return sendGroqMessage(history, userMessage.trim());
    }

    private String tryHandleDatabaseQuestion(String normalizedMessage) throws SQLException {
        if (isTeacherQuizQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countQuizCreatedByTeacher();
            return formatCountResponse(count, "quiz cree par enseignant", "quiz crees par enseignant");
        }
        if (isAdminQuizQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countQuizCreatedByAdmin();
            return formatCountResponse(count, "quiz cree par admin", "quiz crees par admin");
        }
        if (isValidatedQuizQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countValidatedQuiz();
            return formatCountResponse(count, "quiz valide", "quiz valides");
        }
        if (isPendingQuizQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countPendingQuiz();
            return formatCountResponse(count, "quiz en attente", "quiz en attente");
        }
        if (isRejectedQuizQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countRejectedQuiz();
            return formatCountResponse(count, "quiz rejete", "quiz rejetes");
        }
        if (isQuestionCountQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countQuestions();
            return formatCountResponse(count, "question dans la base", "questions dans la base");
        }
        if (isReponseCountQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countReponses();
            return formatCountResponse(count, "reponse dans la base", "reponses dans la base");
        }
        if (isQuizCountQuestion(normalizedMessage)) {
            int count = chatbotDatabaseService.countQuiz();
            return formatCountResponse(count, "quiz dans cette application", "quiz dans cette application");
        }

        return null;
    }

    private String sendGroqMessage(List<ChatMessage> history, String userMessage) throws ChatbotException {
        String apiKey = System.getenv("GROQ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new ChatbotException("La variable d environnement GROQ_API_KEY est absente ou vide.");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", SYSTEM_PROMPT));
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        messages.add(new ChatMessage("user", userMessage));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(messages)))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ChatbotException("Impossible de contacter Groq. Verifiez votre connexion reseau.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChatbotException("La requete vers Groq a ete interrompue.", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ChatbotException(buildHttpErrorMessage(response.statusCode(), response.body()));
        }

        String content = extractAssistantContent(response.body());
        if (content.isBlank()) {
            throw new ChatbotException("Groq n a retourne aucune reponse exploitable.");
        }

        return content;
    }

    private String buildRequestBody(List<ChatMessage> messages) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"model\":\"").append(escapeJson(MODEL_NAME)).append("\",");
        builder.append("\"temperature\":0.7,");
        builder.append("\"messages\":[");

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            if (i > 0) {
                builder.append(",");
            }
            builder.append("{");
            builder.append("\"role\":\"").append(escapeJson(message.role())).append("\",");
            builder.append("\"content\":\"").append(escapeJson(message.content())).append("\"");
            builder.append("}");
        }

        builder.append("]}");
        return builder.toString();
    }

    private String buildHttpErrorMessage(int statusCode, String responseBody) {
        String apiMessage = extractErrorMessage(responseBody);
        if (!apiMessage.isBlank()) {
            return "Erreur Groq (" + statusCode + ") : " + apiMessage;
        }
        return "Erreur Groq (" + statusCode + "). La reponse du service est invalide ou indisponible.";
    }

    private String extractAssistantContent(String responseBody) {
        String safeBody = responseBody == null ? "" : responseBody;
        int choicesIndex = safeBody.indexOf("\"choices\"");
        if (choicesIndex < 0) {
            return "";
        }

        int messageIndex = safeBody.indexOf("\"message\"", choicesIndex);
        if (messageIndex < 0) {
            return "";
        }

        int contentIndex = safeBody.indexOf("\"content\"", messageIndex);
        if (contentIndex < 0) {
            return "";
        }

        return extractJsonStringValue(safeBody, contentIndex + "\"content\"".length()).trim();
    }

    private String extractErrorMessage(String responseBody) {
        String safeBody = responseBody == null ? "" : responseBody;

        int errorIndex = safeBody.indexOf("\"error\"");
        if (errorIndex >= 0) {
            int messageIndex = safeBody.indexOf("\"message\"", errorIndex);
            if (messageIndex >= 0) {
                return extractJsonStringValue(safeBody, messageIndex + "\"message\"".length()).trim();
            }
        }

        int messageIndex = safeBody.indexOf("\"message\"");
        if (messageIndex >= 0) {
            return extractJsonStringValue(safeBody, messageIndex + "\"message\"".length()).trim();
        }

        return "";
    }

    private String extractJsonStringValue(String json, int startIndex) {
        if (json == null || startIndex < 0 || startIndex >= json.length()) {
            return "";
        }

        int colonIndex = json.indexOf(':', startIndex);
        if (colonIndex < 0) {
            return "";
        }

        int firstQuoteIndex = findNextQuote(json, colonIndex + 1);
        if (firstQuoteIndex < 0) {
            return "";
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = firstQuoteIndex + 1; i < json.length(); i++) {
            char current = json.charAt(i);
            if (escaped) {
                value.append('\\').append(current);
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                return unescapeJson(value.toString());
            }
            value.append(current);
        }

        return "";
    }

    private int findNextQuote(String text, int fromIndex) {
        for (int i = Math.max(0, fromIndex); i < text.length(); i++) {
            if (text.charAt(i) == '"') {
                return i;
            }
        }
        return -1;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String result = value
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\/", "/")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        return result.trim();
    }

    private boolean isQuizCountQuestion(String message) {
        return containsAny(message, "combien de quiz", "nombre total de quiz", "total de quiz")
                && !containsAny(message, "valide", "valides", "en attente", "attente", "rejete", "rejetes", "admin", "enseignant", "teacher");
    }

    private boolean isQuestionCountQuestion(String message) {
        return containsAny(message, "combien de question", "nombre de question", "total de question");
    }

    private boolean isReponseCountQuestion(String message) {
        return containsAny(message, "combien de reponse", "nombre de reponse", "total de reponse");
    }

    private boolean isValidatedQuizQuestion(String message) {
        return containsAny(message, "quiz valide", "quiz valides", "quiz validees");
    }

    private boolean isPendingQuizQuestion(String message) {
        return containsAny(message, "quiz en attente", "quiz attente");
    }

    private boolean isRejectedQuizQuestion(String message) {
        return containsAny(message, "quiz rejete", "quiz rejetes", "quiz rejetees");
    }

    private boolean isAdminQuizQuestion(String message) {
        return containsAny(message, "quiz crees par admin", "quiz cree par admin", "quiz admin");
    }

    private boolean isTeacherQuizQuestion(String message) {
        return containsAny(message, "quiz crees par enseignant", "quiz cree par enseignant", "quiz enseignant", "quiz crees par teacher", "quiz cree par teacher");
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private String formatCountResponse(int count, String singularLabel, String pluralLabel) {
        if (count == 1) {
            return "Il y a " + count + " " + singularLabel + ".";
        }
        return "Il y a " + count + " " + pluralLabel + ".";
    }

    private String normalize(String message) {
        String safeMessage = message == null ? "" : message.trim().toLowerCase(Locale.ROOT);
        if (safeMessage.isBlank()) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(safeMessage, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return withoutAccents
                .replace('?', ' ')
                .replace('!', ' ')
                .replace('.', ' ')
                .replace(',', ' ')
                .replace(':', ' ')
                .replace(';', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record ChatMessage(String role, String content) {
    }

    public static class ChatbotException extends Exception {
        public ChatbotException(String message) {
            super(message);
        }

        public ChatbotException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
