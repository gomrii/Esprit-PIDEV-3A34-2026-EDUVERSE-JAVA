package Controllers;

import Services.TranslationService;
import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.ServiceCours;
import Services.TTSService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherChapitre {

    @FXML private VBox chaptersListContainer;
    @FXML private Label contentChapterTitle, chapterContentArea;
    @FXML private VBox mediaContainer, footerActions;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceCours serviceCours = new ServiceCours();

    private Chapitre selectedChapter;
    private Button lastSelectedButton = null;
    private javafx.scene.media.MediaPlayer currentMediaPlayer;

    @FXML
    public void initialize() {
        loadNavigationMenu();
    }

    public void refreshList() {
        loadNavigationMenu();
    }

    void loadNavigationMenu() {
        try {
            List<Cours> listCours = serviceCours.display();
            chaptersListContainer.getChildren().clear();

            for (Cours cours : listCours) {
                Label coursHeader = new Label(cours.getTitle().toUpperCase());
                coursHeader.setFont(Font.font("System", FontWeight.BOLD, 12));
                coursHeader.setStyle("-fx-text-fill: #4B398E; -fx-padding: 15 0 5 10;");
                chaptersListContainer.getChildren().add(coursHeader);

                List<Chapitre> chapitres = serviceChapitre.getChapitresByCoursId(cours.getId());
                for (int i = 0; i < chapitres.size(); i++) {
                    Chapitre ch = chapitres.get(i);
                    Button btn = new Button((i + 1) + ". " + ch.getTitle());
                    setupChapterButton(btn, ch);
                    chaptersListContainer.getChildren().add(btn);
                }
                chaptersListContainer.getChildren().add(new Separator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupChapterButton(Button btn, Chapitre ch) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 20));
        btn.setCursor(Cursor.HAND);

        String styleNormal = "-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-background-radius: 8;";
        String styleSelected = "-fx-background-color: #F4ECF7; -fx-border-color: #8E44AD; -fx-background-radius: 8; -fx-font-weight: bold; -fx-text-fill: #8E44AD;";

        btn.setStyle(styleNormal);
        btn.setOnAction(e -> {
            if (lastSelectedButton != null) lastSelectedButton.setStyle(styleNormal);
            btn.setStyle(styleSelected);
            lastSelectedButton = btn;
            showContent(ch);
        });
    }

    private void showContent(Chapitre ch) {
        this.selectedChapter = ch;
        contentChapterTitle.setText(ch.getTitle());
        chapterContentArea.setText(ch.getContenu());

        footerActions.setVisible(true);
        mediaContainer.getChildren().clear();

        VBox mainTools = new VBox(15);
        mainTools.setAlignment(Pos.CENTER_LEFT);
        mainTools.setPadding(new Insets(10));

        // --- LIGNE 1 : CONTRÔLES AUDIO ---
        HBox audioBox = new HBox(10);
        audioBox.setAlignment(Pos.CENTER_LEFT);

        Button btnPlay = new Button("▶ Lire");
        Button btnPause = new Button("⏸ Pause");
        Button btnStop = new Button("⏹ Stop");

        btnPlay.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
        btnPause.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
        btnStop.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");

        btnPlay.setOnAction(e -> {
            if (currentMediaPlayer != null && currentMediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                currentMediaPlayer.play();
            } else {
                if (currentMediaPlayer != null) currentMediaPlayer.stop();
                TTSService tts = new TTSService();
                currentMediaPlayer = tts.lireTexte(chapterContentArea.getText());
                if (currentMediaPlayer != null) currentMediaPlayer.play();
            }
        });

        btnPause.setOnAction(e -> { if (currentMediaPlayer != null) currentMediaPlayer.pause(); });
        btnStop.setOnAction(e -> { if (currentMediaPlayer != null) currentMediaPlayer.stop(); });

        audioBox.getChildren().addAll(btnPlay, btnPause, btnStop);

        // --- LIGNE 2 : TRADUCTION ---
        HBox translationBox = new HBox(10);
        translationBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTrad = new Label("Traduire en : ");
        ComboBox<String> langSelector = new ComboBox<>();
        langSelector.getItems().addAll("Français", "English", "العربية", "Español");
        langSelector.setStyle("-fx-background-radius: 10;");

        langSelector.setOnAction(e -> {
            String code = switch (langSelector.getValue()) {
                case "English" -> "en";
                case "العربية" -> "ar";
                case "Español" -> "es";
                default -> "fr";
            };

            if (code.equals("fr")) {
                chapterContentArea.setText(ch.getContenu());
            } else {
                TranslationService ts = new TranslationService();
                chapterContentArea.setText(ts.traduire(ch.getContenu(), code));
            }
        });

        translationBox.getChildren().addAll(lblTrad, langSelector);

        mainTools.getChildren().addAll(audioBox, translationBox);
        mediaContainer.getChildren().add(mainTools);
    }

    @FXML
    private void handleDelete() {
        if (selectedChapter == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer : " + selectedChapter.getTitle() + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    serviceChapitre.delete(selectedChapter);
                    resetDisplay();
                    loadNavigationMenu();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    private void resetDisplay() {
        this.selectedChapter = null;
        this.contentChapterTitle.setText("Sélectionnez un chapitre");
        this.chapterContentArea.setText("");
        this.mediaContainer.getChildren().clear();
        this.footerActions.setVisible(false);

        if (lastSelectedButton != null) {
            lastSelectedButton.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-background-radius: 8;");
            lastSelectedButton = null;
        }
    }

    @FXML
    public void handleEdit(ActionEvent actionEvent) {
        if (selectedChapter == null) {
            // Optionnel : Alerte si rien n'est sélectionné
            return;
        }

        // On utilise le Dashboard pour charger la vue au centre
        MainDashboardController.getInstance().loadViewWithData(
                "ModifierChapitre.fxml",
                "Modifier le Chapitre : " + selectedChapter.getTitle(),
                (Object ctrl) -> {
                    ModifierChapitre controller = (ModifierChapitre) ctrl;

                    // 1. On envoie les données du chapitre
                    controller.setChapitreData(selectedChapter);

                    // 2. On lie le contrôleur parent (celui-ci) pour le rafraîchissement
                    controller.setParentController(this);
                }
        );
    }

    // --- NAVIGATION VIA DASHBOARD (GARDE LA SIDEBAR) ---

    @FXML
    void gototcours(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AffichierCoursAdmin.fxml", "Gestion des Cours");
    }

    @FXML
    void gototcha(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AjouterChapitre.fxml", "Nouveau Chapitre");
    }
}