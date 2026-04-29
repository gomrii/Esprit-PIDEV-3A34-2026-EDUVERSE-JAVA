package com.elearning.controller;

import com.elearning.entity.AIConversation;
import com.elearning.dao.AIConversationDAO;
import com.elearning.service.AIAssistantService;
import com.elearning.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller de l'assistant IA Eduverse.
 *
 * Interface style messagerie (bulles gauche/droite).
 * L'IA reçoit les vraies données BDD avant chaque réponse.
 * L'historique des conversations est chargé depuis la BDD au démarrage.
 * Tous les appels API se font dans des Task (thread séparé).
 */
public class AIAssistantController implements Initializable {

    // -------------------------------------------------------
    // Éléments FXML
    // -------------------------------------------------------
    @FXML private VBox              messagesContainer;  // conteneur des bulles
    @FXML private ScrollPane        scrollPane;          // scroll des messages
    @FXML private TextField         inputField;          // saisie utilisateur
    @FXML private Button            btnEnvoyer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label             labelStats;          // stats résumées en en-tête

    // Chips de questions rapides
    @FXML private Button chipResume;
    @FXML private Button chipEnAttente;
    @FXML private Button chipBloques;
    @FXML private Button chipSuggestions;

    // -------------------------------------------------------
    // Services
    // -------------------------------------------------------
    private final AIAssistantService  aiService  = new AIAssistantService();
    private final AIConversationDAO   convDAO    = new AIConversationDAO();

    // -------------------------------------------------------
    // initialize()
    // -------------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (loadingIndicator != null) loadingIndicator.setVisible(false);

        // Message de bienvenue
        afficherMessageIA("👋 Bonjour ! Je suis votre assistant IA Eduverse.\n"
                + "Je suis connecté à votre base de données en temps réel.\n"
                + "Posez-moi n'importe quelle question sur vos utilisateurs !");

        // Charger les 3 dernières conversations depuis la BDD
        chargerHistorique();

        // Scroll automatique vers le bas à chaque nouveau message
        if (messagesContainer != null) {
            messagesContainer.heightProperty().addListener((obs, old, newH) -> {
                if (scrollPane != null) scrollPane.setVvalue(1.0);
            });
        }

        // Enter pour envoyer
        if (inputField != null) {
            inputField.setOnAction(e -> handleEnvoyer(null));
        }
    }

    // -------------------------------------------------------
    // Envoi d'une question
    // -------------------------------------------------------

    @FXML
    private void handleEnvoyer(ActionEvent event) {
        String question = inputField != null ? inputField.getText().trim() : "";
        if (question.isBlank()) return;

        // Afficher la bulle de l'utilisateur
        afficherMessageUtilisateur(question);

        // Vider le champ
        if (inputField != null) inputField.clear();

        // Désactiver le bouton + afficher loader
        setBusy(true);

        // Récupérer l'ID de l'admin connecté
        int adminId = SessionManager.getInstance().getUtilisateurConnecte() != null
                    ? SessionManager.getInstance().getUtilisateurConnecte().getId()
                    : 1;

        // Appel IA dans un Task
        String finalQuestion = question;
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return aiService.poserQuestion(finalQuestion, adminId);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            setBusy(false);
            afficherMessageIA(task.getValue());
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            setBusy(false);
            String err = task.getException() != null
                       ? task.getException().getMessage()
                       : "Erreur inconnue";
            afficherMessageIA("⚠ Impossible de joindre l'assistant IA.\n"
                    + "Vérifiez votre clé API Anthropic.\nErreur : " + err);
        }));

        new Thread(task).start();
    }

    // -------------------------------------------------------
    // Chips de questions rapides
    // -------------------------------------------------------

    @FXML private void handleResume(ActionEvent e)      { envoyerQuestionRapide("Donne-moi un résumé complet des utilisateurs de la plateforme."); }
    @FXML private void handleEnAttente(ActionEvent e)   { envoyerQuestionRapide("Quels utilisateurs sont en attente d'approbation ?"); }
    @FXML private void handleBloques(ActionEvent e)     { envoyerQuestionRapide("Qui sont les comptes bloqués ? Y a-t-il des problèmes à signaler ?"); }
    @FXML private void handleSuggestions(ActionEvent e) { envoyerQuestionRapide("Quelles actions me recommandes-tu de faire en priorité aujourd'hui ?"); }

    private void envoyerQuestionRapide(String question) {
        if (inputField != null) inputField.setText(question);
        handleEnvoyer(null);
    }

    // -------------------------------------------------------
    // Chargement de l'historique
    // -------------------------------------------------------

    private void chargerHistorique() {
        if (SessionManager.getInstance().getUtilisateurConnecte() == null) return;
        int adminId = SessionManager.getInstance().getUtilisateurConnecte().getId();

        new Thread(() -> {
            List<AIConversation> historique = convDAO.getHistorique(adminId, 3);
            if (historique.isEmpty()) return;

            Platform.runLater(() -> {
                // Afficher un séparateur
                Label sep = new Label("── Conversations précédentes ──");
                sep.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
                sep.setMaxWidth(Double.MAX_VALUE);
                sep.setAlignment(javafx.geometry.Pos.CENTER);
                if (messagesContainer != null) messagesContainer.getChildren().add(0, sep);

                // Afficher en ordre chronologique (historique est DESC)
                for (int i = historique.size() - 1; i >= 0; i--) {
                    AIConversation conv = historique.get(i);
                    afficherMessageUtilisateur(conv.getQuestion());
                    afficherMessageIA(conv.getReponse());
                }

                Label sep2 = new Label("── Nouvelle conversation ──");
                sep2.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
                sep2.setMaxWidth(Double.MAX_VALUE);
                sep2.setAlignment(javafx.geometry.Pos.CENTER);
                if (messagesContainer != null) messagesContainer.getChildren().add(sep2);
            });
        }).start();
    }

    // -------------------------------------------------------
    // Création des bulles de messages
    // -------------------------------------------------------

    /**
     * Affiche une bulle de message de l'utilisateur (droite, bleu).
     */
    private void afficherMessageUtilisateur(String texte) {
        Label bubble = creerBulle(texte, "#3498db", "white", Pos.CENTER_RIGHT);
        HBox box = new HBox(bubble);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(3, 10, 3, 50));
        if (messagesContainer != null) messagesContainer.getChildren().add(box);
    }

    /**
     * Affiche une bulle de message de l'IA (gauche, gris clair).
     */
    private void afficherMessageIA(String texte) {
        Label bubble = creerBulle(texte, "#ecf0f1", "#2c3e50", Pos.CENTER_LEFT);
        HBox box = new HBox(bubble);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(3, 50, 3, 10));
        if (messagesContainer != null) messagesContainer.getChildren().add(box);
    }

    private Label creerBulle(String texte, String bgColor, String textColor, Pos pos) {
        Label label = new Label(texte);
        label.setWrapText(true);
        label.setMaxWidth(380);
        label.setPadding(new Insets(10, 14, 10, 14));
        label.setStyle(
                "-fx-background-color: " + bgColor + ";"
              + "-fx-text-fill: " + textColor + ";"
              + "-fx-background-radius: 18;"
              + "-fx-font-size: 13px;"
        );
        return label;
    }

    // -------------------------------------------------------
    // État chargement
    // -------------------------------------------------------

    private void setBusy(boolean busy) {
        if (btnEnvoyer     != null) btnEnvoyer.setDisable(busy);
        if (inputField     != null) inputField.setDisable(busy);
        if (loadingIndicator != null) loadingIndicator.setVisible(busy);
        if (chipResume     != null) chipResume.setDisable(busy);
        if (chipEnAttente  != null) chipEnAttente.setDisable(busy);
        if (chipBloques    != null) chipBloques.setDisable(busy);
        if (chipSuggestions!= null) chipSuggestions.setDisable(busy);
    }
}
