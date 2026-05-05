package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SaplingService {
    private static final String API_KEY_ENV = "SAPLING_API_KEY";
    private static final String EDITS_URL = "https://api.sapling.ai/api/v1/edits";
    private static final String AI_DETECT_URL = "https://api.sapling.ai/api/v1/aidetect";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GrammarCheckResult checkGrammar(String text) throws SaplingException {
        String safeText = normalizeText(text);
        if (safeText.isBlank()) {
            throw new SaplingException("Veuillez saisir un texte a corriger.");
        }

        String requestBody = buildGrammarRequestBody(safeText);
        logDebug("Sapling grammar text", safeText);

        String responseBody = sendRequest(EDITS_URL, requestBody);
        logDebug("Sapling grammar raw response", responseBody);

        JsonNode root = parseJson(responseBody);
        List<GrammarEdit> edits = parseGrammarEdits(root.path("edits"));
        String correctedText = root.path("applied_text").asText("").trim();

        if (correctedText.isBlank()) {
            correctedText = applyEdits(safeText, edits);
        }
        if (correctedText.isBlank()) {
            correctedText = safeText;
        }

        logDebug("Sapling grammar edits count", String.valueOf(edits.size()));
        for (GrammarEdit edit : edits) {
            logDebug("Sapling grammar edit", edit.toDebugString());
        }

        return new GrammarCheckResult(safeText, correctedText, edits);
    }

    public AiDetectionResult detectAiContent(String text) throws SaplingException {
        String safeText = normalizeText(text);
        if (safeText.isBlank()) {
            throw new SaplingException("Veuillez saisir un texte a analyser.");
        }

        String requestBody = buildAiDetectRequestBody(safeText);
        logDebug("Sapling AI detect text", safeText);

        String responseBody = sendRequest(AI_DETECT_URL, requestBody);
        logDebug("Sapling AI detect raw response", responseBody);

        JsonNode root = parseJson(responseBody);
        if (!root.has("score") || root.path("score").isNull()) {
            throw new SaplingException("La reponse Sapling est vide ou invalide.");
        }

        double score = root.path("score").asDouble(Double.NaN);
        if (Double.isNaN(score)) {
            throw new SaplingException("La reponse Sapling est vide ou invalide.");
        }

        return new AiDetectionResult(score, buildVerdict(score));
    }

    private String buildGrammarRequestBody(String text) throws SaplingException {
        try {
            return objectMapper.writeValueAsString(new GrammarRequest(
                    readApiKey(),
                    text,
                    UUID.randomUUID().toString(),
                    "auto",
                    true
            ));
        } catch (IOException e) {
            throw new SaplingException("Impossible de preparer la requete Sapling.", e);
        }
    }

    private String buildAiDetectRequestBody(String text) throws SaplingException {
        try {
            return objectMapper.writeValueAsString(new AiDetectRequest(
                    readApiKey(),
                    text,
                    true
            ));
        } catch (IOException e) {
            throw new SaplingException("Impossible de preparer la requete Sapling.", e);
        }
    }

    private JsonNode parseJson(String responseBody) throws SaplingException {
        try {
            return objectMapper.readTree(responseBody);
        } catch (IOException e) {
            throw new SaplingException("La reponse JSON de Sapling est invalide.", e);
        }
    }

    private String sendRequest(String url, String body) throws SaplingException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new SaplingException("Impossible de contacter Sapling.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SaplingException("La requete Sapling a ete interrompue.", e);
        }

        String responseBody = response.body() == null ? "" : response.body().trim();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw buildHttpException(response.statusCode(), responseBody);
        }
        if (responseBody.isBlank()) {
            throw new SaplingException("La reponse Sapling est vide.");
        }
        return responseBody;
    }

    private SaplingException buildHttpException(int statusCode, String responseBody) {
        String apiMessage = extractApiMessage(responseBody);
        String loweredMessage = apiMessage.toLowerCase(Locale.ROOT);

        if (statusCode == 401 || statusCode == 403
                || (statusCode == 400 && (loweredMessage.contains("key") || loweredMessage.contains("auth")))) {
            return new SaplingException("Cle API Sapling invalide.");
        }
        if (statusCode >= 500) {
            return new SaplingException("Le service Sapling est momentanement indisponible.");
        }
        if (!apiMessage.isBlank()) {
            return new SaplingException("Erreur Sapling (" + statusCode + ") : " + apiMessage);
        }
        return new SaplingException("Erreur HTTP Sapling (" + statusCode + ").");
    }

    private String extractApiMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody == null ? "" : responseBody);
            if (root.hasNonNull("msg")) {
                return root.path("msg").asText("");
            }
            if (root.hasNonNull("message")) {
                return root.path("message").asText("");
            }
        } catch (IOException ignored) {
        }
        return "";
    }

    private String readApiKey() throws SaplingException {
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            throw new SaplingException("Cle API Sapling absente.");
        }
        return apiKey.trim();
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String buildVerdict(double score) {
        if (score >= 0.5d) {
            return "Texte probablement genere par IA";
        }
        return "Texte probablement humain";
    }

    private List<GrammarEdit> parseGrammarEdits(JsonNode editsNode) {
        if (editsNode == null || !editsNode.isArray() || editsNode.isEmpty()) {
            return List.of();
        }

        List<GrammarEdit> edits = new ArrayList<>();
        for (JsonNode node : editsNode) {
            int sentenceStart = node.path("sentence_start").asInt(-1);
            int start = node.path("start").asInt(-1);
            int end = node.path("end").asInt(-1);
            if (sentenceStart < 0 || start < 0 || end < start) {
                continue;
            }

            edits.add(new GrammarEdit(
                    sentenceStart + start,
                    sentenceStart + end,
                    node.path("replacement").asText(""),
                    node.path("general_error_type").asText("")
            ));
        }
        edits.sort(Comparator.comparingInt(GrammarEdit::absoluteStart));
        return edits;
    }

    private String applyEdits(String originalText, List<GrammarEdit> edits) {
        if (originalText == null || originalText.isBlank() || edits == null || edits.isEmpty()) {
            return originalText == null ? "" : originalText;
        }

        StringBuilder builder = new StringBuilder(originalText);
        List<GrammarEdit> orderedEdits = new ArrayList<>(edits);
        orderedEdits.sort(Comparator.comparingInt(GrammarEdit::absoluteStart).reversed());

        for (GrammarEdit edit : orderedEdits) {
            int start = Math.max(0, Math.min(edit.absoluteStart(), builder.length()));
            int end = Math.max(start, Math.min(edit.absoluteEnd(), builder.length()));
            builder.replace(start, end, edit.replacement() == null ? "" : edit.replacement());
        }
        return builder.toString();
    }

    private void logDebug(String label, String value) {
        System.out.println("[SaplingService] " + label + ": " + (value == null ? "" : value));
    }

    public record GrammarCheckResult(String originalText, String correctedText, List<GrammarEdit> edits) {
        public boolean hasChanges() {
            return correctedText != null && !correctedText.equals(originalText);
        }

        public boolean hasDetectedCorrections() {
            return edits != null && !edits.isEmpty();
        }
    }

    public record GrammarEdit(int absoluteStart, int absoluteEnd, String replacement, String errorType) {
        public String toDebugString() {
            return "start=" + absoluteStart
                    + ", end=" + absoluteEnd
                    + ", replacement=" + replacement
                    + ", type=" + errorType;
        }
    }

    public record AiDetectionResult(double score, String verdict) {
        public double scorePercent() {
            return score * 100d;
        }
    }

    public static class SaplingException extends Exception {
        public SaplingException(String message) {
            super(message);
        }

        public SaplingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record GrammarRequest(
            String key,
            String text,
            String session_id,
            String lang,
            boolean auto_apply
    ) {
    }

    private record AiDetectRequest(
            String key,
            String text,
            boolean sent_scores
    ) {
    }
}
