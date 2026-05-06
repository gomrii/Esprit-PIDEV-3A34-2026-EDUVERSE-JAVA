package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiTranslationService {
    private static final String GEMINI_API_KEY_ENV = "GEMINI_API_KEY";
    private static final String GOOGLE_API_KEY_ENV = "GOOGLE_API_KEY";
    private static final String MISSING_API_KEY_MESSAGE =
            "Aucune cle Gemini detectee. Verifiez GOOGLE_API_KEY ou GEMINI_API_KEY, puis relancez IntelliJ et l'application.";
    private static final String INVALID_API_KEY_MESSAGE = "Cle API Gemini invalide ou acces refuse.";
    private static final String QUOTA_EXCEEDED_MESSAGE =
            "Quota Gemini depasse. Reessayez plus tard ou verifiez vos limites d'utilisation dans Google AI Studio.";
    private static final String NETWORK_ERROR_MESSAGE = "Impossible de contacter Gemini. Verifiez votre connexion.";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Le service Gemini est momentanement indisponible.";
    private static final String EMPTY_RESPONSE_MESSAGE = "La reponse Gemini est vide.";
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String GENERATE_CONTENT_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String translateToEnglish(String text) throws GeminiTranslationException {
        return translate(text, "anglais");
    }

    public String translateToFrench(String text) throws GeminiTranslationException {
        return translate(text, "francais");
    }

    private String translate(String text, String targetLanguage) throws GeminiTranslationException {
        String safeText = normalizeText(text);
        if (safeText.isBlank()) {
            throw new GeminiTranslationException("Veuillez saisir un texte a traduire.");
        }

        String prompt = "Traduis ce texte en " + targetLanguage
                + " sans ajouter d'explication, retourne uniquement la traduction : " + safeText;

        String requestBody = buildRequestBody(prompt);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GENERATE_CONTENT_URL))
                .timeout(Duration.ofSeconds(45))
                .header("x-goog-api-key", readApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logDebug("Erreur reseau Gemini: " + e.getMessage());
            throw new GeminiTranslationException(NETWORK_ERROR_MESSAGE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeminiTranslationException("La requete Gemini a ete interrompue.", e);
        }

        String responseBody = response.body() == null ? "" : response.body().trim();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw buildHttpException(response.statusCode(), responseBody);
        }
        if (responseBody.isBlank()) {
            logDebug("Gemini a retourne une reponse vide.");
            throw new GeminiTranslationException(EMPTY_RESPONSE_MESSAGE);
        }

        return extractTranslatedText(responseBody, safeText);
    }

    private String buildRequestBody(String prompt) throws GeminiTranslationException {
        try {
            return objectMapper.writeValueAsString(new GenerateContentRequest(
                    new Content[]{
                            new Content("user", new Part[]{new Part(prompt)})
                    },
                    new GenerationConfig(0.2d, "text/plain")
            ));
        } catch (IOException e) {
            throw new GeminiTranslationException("Impossible de preparer la requete Gemini.", e);
        }
    }

    private String extractTranslatedText(String responseBody, String originalText) throws GeminiTranslationException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                logDebug("Gemini n'a retourne aucun candidat. Payload: " + responseBody);
                throw new GeminiTranslationException(EMPTY_RESPONSE_MESSAGE);
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

            String translatedText = builder.toString().trim();
            if (translatedText.isBlank()) {
                logDebug("Gemini a retourne un texte traduit vide. Payload: " + responseBody);
                throw new GeminiTranslationException(EMPTY_RESPONSE_MESSAGE);
            }

            if (translatedText.equals(originalText)) {
                return translatedText;
            }

            return translatedText;
        } catch (IOException e) {
            throw new GeminiTranslationException("La reponse JSON de Gemini est invalide.", e);
        }
    }

    private GeminiTranslationException buildHttpException(int statusCode, String responseBody) {
        String apiMessage = extractApiMessage(responseBody);
        logDebug("Erreur HTTP Gemini " + statusCode + " - message brut: "
                + (apiMessage.isBlank() ? "<aucun message>" : apiMessage));

        if (statusCode == 401 || statusCode == 403) {
            return new GeminiTranslationException(INVALID_API_KEY_MESSAGE);
        }
        if (statusCode == 429) {
            return new GeminiTranslationException(QUOTA_EXCEEDED_MESSAGE);
        }
        if (statusCode >= 500) {
            return new GeminiTranslationException(SERVICE_UNAVAILABLE_MESSAGE);
        }
        if (!apiMessage.isBlank()) {
            return new GeminiTranslationException("Erreur Gemini (" + statusCode + ") : " + apiMessage);
        }
        return new GeminiTranslationException("Erreur HTTP Gemini (" + statusCode + ").");
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

    private String readApiKey() throws GeminiTranslationException {
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
        throw new GeminiTranslationException(MISSING_API_KEY_MESSAGE);
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim();
    }

    private void logDebug(String message) {
        System.out.println("DEBUG: " + message);
    }

    public static class GeminiTranslationException extends Exception {
        public GeminiTranslationException(String message) {
            super(message);
        }

        public GeminiTranslationException(String message, Throwable cause) {
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
