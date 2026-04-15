package controller;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import Services.ServiceCours;
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

    @FXML
    public void initialize() {
        loadNavigationMenu();
    }

    public void refreshList() {
        loadNavigationMenu(); // Réutilise votre méthode existante qui lit la DB
    }

    void loadNavigationMenu() {
        try {
            List<Cours> listCours = serviceCours.display();
            chaptersListContainer.getChildren().clear();

            for (Cours cours : listCours) {
                // En-tête du cours
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
        this.selectedChapter = ch; // Crucial pour le bouton supprimer/modifier

        contentChapterTitle.setText(ch.getTitle());
        chapterContentArea.setText(ch.getContenu()); // Envoie le texte vers le Label

        // Rend le footer visible uniquement quand un chapitre est chargé
        footerActions.setVisible(true);

        mediaContainer.getChildren().clear();


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
        // 1. On oublie le chapitre précédemment sélectionné
        this.selectedChapter = null;

        // 2. On remet les textes par défaut
        this.contentChapterTitle.setText("Sélectionnez un chapitre");
        this.chapterContentArea.setText("");

        // 3. On vide le conteneur des boutons (Vidéo/PDF)
        this.mediaContainer.getChildren().clear();

        // 4. On cache la section "Fin de leçon" (Modifier/Supprimer)
        this.footerActions.setVisible(false);

        // 5. Optionnel : On déselectionne le dernier bouton dans la barre latérale
        if (lastSelectedButton != null) {
            lastSelectedButton.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-background-radius: 8;");
            lastSelectedButton = null;
        }
    }

    @FXML
    public void handleEdit(ActionEvent actionEvent) {
        try {
            // 1. Vérifier si un chapitre est sélectionné
            // Si tu utilises une TableView, remplace 'selectedChapter' par table.getSelectionModel().getSelectedItem()
            if (selectedChapter == null) {
                showAlert("Attention", "Veuillez sélectionner un chapitre à modifier.", Alert.AlertType.WARNING);
                return;
            }

            // 2. Charger le fichier FXML de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierChapitre.fxml"));
            Parent root = loader.load();

            // 3. Récupérer le contrôleur et passer l'objet Chapitre
            ModifierChapitre controller = loader.getController();
            controller.setChapitreData(selectedChapter); // On envoie l'objet au formulaire

            // IMPORTANT : Passer 'this' pour que le refresh se fasse après l'update
            controller.setParentController(this);

            // 4. Ouvrir la fenêtre de modification
            Stage stage = new Stage();
            stage.setTitle("Modifier le Chapitre : " + selectedChapter.getTitle());
            stage.setScene(new Scene(root));

            // On peut utiliser showAndWait() pour bloquer l'interaction avec la fenêtre principale
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur de chargement du FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Petite méthode utilitaire pour les alertes
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void gototcours(ActionEvent event) {
        try {
            // Chargement du fichier FXML de l'administration des cours
            // Vérifie bien que le nom est exactement "AffichierCoursAdmin.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierCoursAdmin.fxml"));
            Parent root = loader.load();

            // Récupération du Stage (la fenêtre) actuel
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Création et application de la nouvelle scène
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Optionnel : centrer la fenêtre si la taille change
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur de navigation vers Admin Cours : " + e.getMessage());
            e.printStackTrace();

            // Alerte visuelle en cas d'erreur (fichier manquant par exemple)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger la page AffichierCoursAdmin.");
            alert.show();
        }
    }

    @FXML
    void gototcha(ActionEvent event) {
        try {
            // Chargement du fichier FXML de l'administration des cours
            // Vérifie bien que le nom est exactement "AffichierCoursAdmin.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterChapitre.fxml"));
            Parent root = loader.load();

            // Récupération du Stage (la fenêtre) actuel
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Création et application de la nouvelle scène
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Optionnel : centrer la fenêtre si la taille change
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur de navigation vers Admin Cours : " + e.getMessage());
            e.printStackTrace();

            // Alerte visuelle en cas d'erreur (fichier manquant par exemple)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger la page AffichierCoursAdmin.");
            alert.show();
        }
    }



}
