package controller;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.ServiceCours;
import Services.TTSService; // Assure-toi que l'import est correct
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherChapitre {

    @FXML
    private VBox chaptersListContainer;
    @FXML
    private Label contentChapterTitle, chapterContentArea;
    @FXML
    private VBox mediaContainer, footerActions;

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

        // Conteneur pour les boutons audio
        HBox audioControls = new HBox(10);
        audioControls.setAlignment(Pos.CENTER);
        audioControls.setPadding(new Insets(10));

        // Bouton LIRE / REPRENDRE
        Button btnPlay = new Button("▶ Lire");
        btnPlay.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");

        // Bouton PAUSE
        Button btnPause = new Button("⏸ Pause");
        btnPause.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");

        // Bouton STOP
        Button btnStop = new Button("⏹ Stop");
        btnStop.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");

        // --- LOGIQUE DES BOUTONS ---

        btnPlay.setOnAction(e -> {
            if (currentMediaPlayer != null && currentMediaPlayer.getStatus() == javafx.scene.media.MediaPlayer.Status.PAUSED) {
                currentMediaPlayer.play();
            } else {
                // Si rien n'est lancé, on génère le son
                if (currentMediaPlayer != null) currentMediaPlayer.stop();

                TTSService tts = new TTSService();
                // On récupère le MediaPlayer créé par le service
                // Note : Il faut modifier ton TTSService pour qu'il retourne le MediaPlayer
                currentMediaPlayer = tts.lireTexte(ch.getContenu());
                currentMediaPlayer.play();
            }
        });

        btnPause.setOnAction(e -> {
            if (currentMediaPlayer != null) currentMediaPlayer.pause();
        });

        btnStop.setOnAction(e -> {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.stop();
            }
        });

        audioControls.getChildren().addAll(btnPlay, btnPause, btnStop);
        mediaContainer.getChildren().add(audioControls);
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
        try {
            if (selectedChapter == null) {
                showAlert("Attention", "Veuillez sélectionner un chapitre à modifier.", Alert.AlertType.WARNING);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierChapitre.fxml"));
            Parent root = loader.load();

            ModifierChapitre controller = loader.getController();
            controller.setChapitreData(selectedChapter);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Chapitre : " + selectedChapter.getTitle());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void gototcours(ActionEvent event) {
        navigate(event, "/AffichierCoursAdmin.fxml");
    }

    @FXML
    void gototcha(ActionEvent event) {
        navigate(event, "/AjouterChapitre.fxml");
    }

    // Méthode de navigation générique pour éviter la répétition
    private void navigate(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}