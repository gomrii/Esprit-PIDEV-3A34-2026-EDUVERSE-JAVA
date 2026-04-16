package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Quiz;
import edu.connexion3a36.services.DashboardStatsService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentQuizResultController {
    private static final DateTimeFormatter PDF_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final float PAGE_MARGIN = 42f;
    private static final float COLOR_SCALE = 255f;
    private static final DashboardStatsService DASHBOARD_STATS_SERVICE = new DashboardStatsService();

    @FXML
    private Label quizTitleLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label percentageLabel;
    @FXML
    private Label answeredLabel;
    @FXML
    private Label summaryLabel;

    private Quiz currentQuiz;
    private double currentScore;
    private double currentTotalPossibleScore;
    private int currentAnsweredQuestions;
    private int currentTotalQuestions;
    private boolean currentTimeExpired;

    public void setResult(Quiz quiz, double score, double totalPossibleScore, int answeredQuestions, int totalQuestions) {
        setResult(quiz, score, totalPossibleScore, answeredQuestions, totalQuestions, false);
    }

    public void setResult(Quiz quiz, double score, double totalPossibleScore, int answeredQuestions, int totalQuestions, boolean timeExpired) {
        currentQuiz = quiz;
        currentScore = score;
        currentTotalPossibleScore = totalPossibleScore;
        currentAnsweredQuestions = answeredQuestions;
        currentTotalQuestions = totalQuestions;
        currentTimeExpired = timeExpired;
        DASHBOARD_STATS_SERVICE.recordStudentAttempt(quiz, score, totalPossibleScore);

        quizTitleLabel.setText(quiz != null ? "Résultat du quiz : " + safeValue(quiz.getTitre()) : "Résultat du quiz");
        scoreLabel.setText("Score final : " + formatScore(score) + " / " + formatScore(totalPossibleScore));
        percentageLabel.setText("Pourcentage : " + formatScore(calculatePercentage()) + "%");
        answeredLabel.setText("Questions répondues : " + answeredQuestions + " / " + totalQuestions);
        summaryLabel.setText(buildSummaryText());
    }

    @FXML
    private void handleDownloadPdf(ActionEvent event) {
        if (currentQuiz == null) {
            ControllerUtils.showWarning("Aucun résultat à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le résultat en PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName(buildDefaultFileName());

        Window window = event.getSource() instanceof Node node ? node.getScene().getWindow() : quizTitleLabel.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(window);
        if (selectedFile == null) {
            return;
        }

        try {
            generatePdf(selectedFile);
            ControllerUtils.showInfo("Le résultat a été enregistré en PDF avec succès.");
        } catch (IOException e) {
            ControllerUtils.showError("La génération du PDF a échoué : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        ControllerUtils.navigateTo((Node) event.getSource(), "/student_quiz_list.fxml");
    }

    private void generatePdf(File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDImageXObject logo = loadLogo(document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float contentWidth = pageWidth - (2 * PAGE_MARGIN);
                float currentY = pageHeight - PAGE_MARGIN;

                drawPageBackground(contentStream, pageWidth, pageHeight);
                currentY = drawHeader(contentStream, logo, pageWidth, currentY);
                currentY -= 22;
                currentY = drawScoreCard(contentStream, PAGE_MARGIN, currentY, contentWidth);
                currentY -= 22;
                currentY = drawDetailsSection(contentStream, PAGE_MARGIN, currentY, contentWidth);
                currentY -= 20;
                currentY = drawStatusSection(contentStream, PAGE_MARGIN, currentY, contentWidth);
                drawFooter(contentStream, PAGE_MARGIN, 34, contentWidth);
            }

            document.save(outputFile);
        }
    }

    private void drawPageBackground(PDPageContentStream contentStream, float pageWidth, float pageHeight) throws IOException {
        setFillColor(contentStream, 246, 249, 252);
        contentStream.addRect(0, 0, pageWidth, pageHeight);
        contentStream.fill();
    }

    private float drawHeader(PDPageContentStream contentStream, PDImageXObject logo, float pageWidth, float topY) throws IOException {
        float headerHeight = 148f;
        float headerBottomY = topY - headerHeight;

        fillRect(contentStream, 0, headerBottomY, pageWidth, headerHeight, 29, 59, 83);
        fillRect(contentStream, PAGE_MARGIN, headerBottomY + 16, pageWidth - (2 * PAGE_MARGIN), 6, 12, 188, 135);

        float logoBoxWidth = 92f;
        float logoBoxHeight = 92f;
        float logoBoxX = PAGE_MARGIN;
        float logoBoxY = headerBottomY + 28f;
        fillRect(contentStream, logoBoxX, logoBoxY, logoBoxWidth, logoBoxHeight, 255, 255, 255);

        if (logo != null) {
            float[] dimensions = fitWithin(logo.getWidth(), logo.getHeight(), 68f, 68f);
            float logoX = logoBoxX + (logoBoxWidth - dimensions[0]) / 2f;
            float logoY = logoBoxY + (logoBoxHeight - dimensions[1]) / 2f;
            contentStream.drawImage(logo, logoX, logoY, dimensions[0], dimensions[1]);
        } else {
            writeText(contentStream, "EV", logoBoxX + 28f, logoBoxY + 35f, FONT_BOLD, 24, 29, 59, 83);
        }

        float textX = logoBoxX + logoBoxWidth + 22f;
        float textY = topY - 40f;
        writeText(contentStream, "EduVerse", textX, textY, FONT_BOLD, 28, 255, 255, 255);
        writeText(contentStream, "Résultat officiel du quiz", textX, textY - 26f, FONT_REGULAR, 14, 222, 233, 242);
        writeText(contentStream, "Document généré automatiquement pour l'espace étudiant", textX, textY - 46f, FONT_REGULAR, 11, 187, 204, 219);

        return headerBottomY;
    }

    private float drawScoreCard(PDPageContentStream contentStream, float x, float topY, float width) throws IOException {
        float cardHeight = 112f;
        float y = topY - cardHeight;

        fillRect(contentStream, x, y, width, cardHeight, 255, 255, 255);
        fillRect(contentStream, x, y + cardHeight - 6f, width, 6f, 52, 152, 219);
        fillRect(contentStream, x + 22f, y + 20f, 124f, 56f, 232, 248, 240);

        writeText(contentStream, "Score final", x + 34f, y + 56f, FONT_BOLD, 13, 27, 127, 71);
        writeText(contentStream, formatScore(currentScore) + " / " + formatScore(currentTotalPossibleScore), x + 34f, y + 34f, FONT_BOLD, 20, 12, 94, 54);

        float centerX = x + 182f;
        writeText(contentStream, "Performance", centerX, y + 62f, FONT_BOLD, 12, 123, 138, 153);
        writeText(contentStream, formatScore(calculatePercentage()) + " %", centerX, y + 34f, FONT_BOLD, 22, 44, 62, 80);

        float rightX = x + width - 168f;
        writeText(contentStream, "Questions répondues", rightX, y + 62f, FONT_BOLD, 12, 123, 138, 153);
        writeText(contentStream, currentAnsweredQuestions + " / " + currentTotalQuestions, rightX, y + 34f, FONT_BOLD, 20, 44, 62, 80);

        return y;
    }

    private float drawDetailsSection(PDPageContentStream contentStream, float x, float topY, float width) throws IOException {
        float sectionHeight = 250f;
        float y = topY - sectionHeight;
        float rowHeight = 28f;
        float labelX = x + 24f;
        float valueX = x + 180f;

        fillRect(contentStream, x, y, width, sectionHeight, 255, 255, 255);
        fillRect(contentStream, x, y + sectionHeight - 44f, width, 44f, 239, 245, 250);
        writeText(contentStream, "Synthèse", x + 24f, y + sectionHeight - 28f, FONT_BOLD, 16, 29, 59, 83);

        String[][] rows = {
                {"Quiz", safeValue(currentQuiz.getTitre())},
                {"Score final", formatScore(currentScore) + " / " + formatScore(currentTotalPossibleScore)},
                {"Pourcentage", formatScore(calculatePercentage()) + " %"},
                {"Questions répondues", currentAnsweredQuestions + " / " + currentTotalQuestions},
                {"Durée", formatDuration()},
                {"Niveau", formatLevel()},
                {"Généré le", LocalDateTime.now().format(PDF_DATE_FORMATTER)}
        };

        float rowY = y + sectionHeight - 68f;
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) {
                strokeLine(contentStream, x + 24f, rowY + 8f, x + width - 24f, rowY + 8f, 224, 231, 237, 0.8f);
            }
            writeText(contentStream, rows[i][0] + " :", labelX, rowY - 12f, FONT_BOLD, 12, 73, 80, 87);
            writeWrappedText(contentStream, rows[i][1], valueX, rowY - 12f, width - 204f, FONT_REGULAR, 12, 52, 73, 94, 16f);
            rowY -= rowHeight;
        }

        return y;
    }

    private float drawStatusSection(PDPageContentStream contentStream, float x, float topY, float width) throws IOException {
        float sectionHeight = 112f;
        float y = topY - sectionHeight;

        fillRect(contentStream, x, y, width, sectionHeight, 255, 255, 255);
        fillRect(contentStream, x, y + sectionHeight - 44f, width, 44f, 239, 245, 250);
        writeText(contentStream, "Statut", x + 24f, y + sectionHeight - 28f, FONT_BOLD, 16, 29, 59, 83);

        fillRect(contentStream, x + 24f, y + 20f, width - 48f, 36f, 232, 248, 240);
        writeWrappedText(contentStream, buildSummaryText(), x + 38f, y + 44f, width - 76f, FONT_REGULAR, 12, 27, 127, 71, 16f);

        return y;
    }

    private void drawFooter(PDPageContentStream contentStream, float x, float y, float width) throws IOException {
        strokeLine(contentStream, x, y + 18f, x + width, y + 18f, 214, 222, 230, 1f);
        writeText(contentStream, "EduVerse • Résultat officiel généré automatiquement", x, y, FONT_REGULAR, 10, 123, 138, 153);
        writeText(contentStream, "Document prêt à être enregistré ou partagé", x + width - 190f, y, FONT_REGULAR, 10, 123, 138, 153);
    }

    private PDImageXObject loadLogo(PDDocument document) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/eduverse-logo.png")) {
            if (inputStream == null) {
                return null;
            }
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
                return null;
            }
            return LosslessFactory.createFromImage(document, bufferedImage);
        }
    }

    private float[] fitWithin(float originalWidth, float originalHeight, float maxWidth, float maxHeight) {
        float ratio = Math.min(maxWidth / originalWidth, maxHeight / originalHeight);
        return new float[]{originalWidth * ratio, originalHeight * ratio};
    }

    private void fillRect(PDPageContentStream contentStream, float x, float y, float width, float height,
                          int red, int green, int blue) throws IOException {
        setFillColor(contentStream, red, green, blue);
        contentStream.addRect(x, y, width, height);
        contentStream.fill();
    }

    private void strokeLine(PDPageContentStream contentStream, float startX, float startY, float endX, float endY,
                            int red, int green, int blue, float lineWidth) throws IOException {
        setStrokeColor(contentStream, red, green, blue);
        contentStream.setLineWidth(lineWidth);
        contentStream.moveTo(startX, startY);
        contentStream.lineTo(endX, endY);
        contentStream.stroke();
    }

    private void writeWrappedText(PDPageContentStream contentStream, String text, float x, float y, float maxWidth,
                                  PDType1Font font, float fontSize, int red, int green, int blue, float lineHeight) throws IOException {
        float currentY = y;
        for (String line : wrapText(text, font, fontSize, maxWidth)) {
            writeText(contentStream, line, x, currentY, font, fontSize, red, green, blue);
            currentY -= lineHeight;
        }
    }

    private void writeText(PDPageContentStream contentStream, String text, float x, float y, PDType1Font font, float fontSize,
                           int red, int green, int blue) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        setFillColor(contentStream, red, green, blue);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void setFillColor(PDPageContentStream contentStream, int red, int green, int blue) throws IOException {
        contentStream.setNonStrokingColor(red / COLOR_SCALE, green / COLOR_SCALE, blue / COLOR_SCALE);
    }

    private void setStrokeColor(PDPageContentStream contentStream, int red, int green, int blue) throws IOException {
        contentStream.setStrokingColor(red / COLOR_SCALE, green / COLOR_SCALE, blue / COLOR_SCALE);
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split("\\s+")) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            float candidateWidth = font.getStringWidth(candidate) / 1000 * fontSize;
            if (candidateWidth <= maxWidth) {
                currentLine.setLength(0);
                currentLine.append(candidate);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                }
                currentLine.setLength(0);
                currentLine.append(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private double calculatePercentage() {
        return currentTotalPossibleScore > 0 ? (currentScore / currentTotalPossibleScore) * 100 : 0;
    }

    private String formatDuration() {
        if (currentQuiz == null || currentQuiz.getDuree() <= 0) {
            return "Non renseignée";
        }
        return currentQuiz.getDuree() + " min";
    }

    private String formatLevel() {
        if (currentQuiz == null || currentQuiz.getLevel() == null || currentQuiz.getLevel().isBlank()) {
            return "Non renseigné";
        }
        return currentQuiz.getLevel();
    }

    private String buildSummaryText() {
        if (currentTimeExpired) {
            return "Temps écoulé. Le quiz a été soumis automatiquement à la fin du chronomètre.";
        }
        if (currentScore > 0) {
            return "Quiz soumis avec succès. Le résultat ci-dessus résume la performance finale de l'étudiant.";
        }
        return "Quiz soumis. Le résultat indique un score nul ou aucune réponse correcte.";
    }

    private String buildDefaultFileName() {
        String quizTitle = currentQuiz != null ? safeValue(currentQuiz.getTitre()) : "quiz";
        return ("resultat-" + quizTitle.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-_]+", "-") + ".pdf")
                .replaceAll("-{2,}", "-");
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatScore(double value) {
        if (Math.floor(value) == value) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
