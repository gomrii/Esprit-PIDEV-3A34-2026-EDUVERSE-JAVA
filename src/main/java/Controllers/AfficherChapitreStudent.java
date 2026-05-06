package Controllers;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.TTSService;
import Services.TranslationService;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherChapitreStudent {

    @FXML private VBox chaptersListContainer;
    @FXML private Label contentChapterTitle, chapterContentArea;
    @FXML private VBox mediaContainer;
    @FXML private VBox footerActions;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private Cours currentCours;
    private Chapitre selectedChapter;
    private MediaPlayer currentMediaPlayer;

    public void setCoursData(Cours cours) {
        this.currentCours = cours;
        loadChapters();
    }

    private void loadChapters() {
        if (currentCours == null) return;
        try {
            chaptersListContainer.getChildren().clear();
            List<Chapitre> chapitres = serviceChapitre.getChapitresByCoursId(currentCours.getId());

            for (int i = 0; i < chapitres.size(); i++) {
                Chapitre ch = chapitres.get(i);
                Button btn = new Button((i + 1) + ". " + ch.getTitle());
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setAlignment(Pos.CENTER_LEFT);
                btn.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-cursor: hand; -fx-padding: 10;");

                btn.setOnAction(e -> showLesson(ch));
                chaptersListContainer.getChildren().add(btn);
            }

            if (!chapitres.isEmpty()) showLesson(chapitres.get(0));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showLesson(Chapitre ch) {
        this.selectedChapter = ch;
        contentChapterTitle.setText(ch.getTitle());
        chapterContentArea.setText(ch.getContenu());

        footerActions.setVisible(true);

        if (currentMediaPlayer != null) currentMediaPlayer.stop();
        setupTools(ch);
    }

    private void setupTools(Chapitre ch) {
        mediaContainer.getChildren().clear();
        HBox tools = new HBox(15);
        tools.setAlignment(Pos.CENTER_LEFT);

        Button btnPlay = new Button("▶ Lire la leçon");
        btnPlay.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
        btnPlay.setOnAction(e -> {
            if (currentMediaPlayer != null) currentMediaPlayer.stop();
            TTSService tts = new TTSService();
            currentMediaPlayer = tts.lireTexte(chapterContentArea.getText());
            if (currentMediaPlayer != null) currentMediaPlayer.play();
        });

        ComboBox<String> lang = new ComboBox<>();
        lang.getItems().addAll("Français", "English", "العربية");
        lang.setPromptText("Traduire");
        lang.setStyle("-fx-background-radius: 8;");

        lang.setOnAction(e -> {
            String selection = lang.getValue();
            String code = selection.equals("English") ? "en" : selection.equals("العربية") ? "ar" : "fr";

            if (code.equals("fr")) {
                chapterContentArea.setText(ch.getContenu());
            } else {
                TranslationService ts = new TranslationService();
                chapterContentArea.setText(ts.traduire(ch.getContenu(), code));
            }
        });

        tools.getChildren().addAll(btnPlay, lang);
        mediaContainer.getChildren().add(tools);
    }

    @FXML
    private void handleGeneratePDF(ActionEvent event) {
        if (selectedChapter == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(selectedChapter.getTitle() + ".pdf");
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            Document document = new Document(PageSize.A4);
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                document.add(new Paragraph(selectedChapter.getTitle(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20)));
                document.add(new Paragraph("\n" + chapterContentArea.getText()));
                document.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // --- MODIFICATION ICI : NAVIGATION VIA DASHBOARD POUR GARDER LA SIDEBAR ---
    @FXML
    public void handleBack(ActionEvent event) {
        if (currentMediaPlayer != null) currentMediaPlayer.stop();

        // On utilise l'instance du Dashboard pour recharger la vue des cours sans perdre la sidebar
        MainDashboardController.getInstance().loadView("AffichierCoursStudent.fxml", "Mes Cours");
    }
}