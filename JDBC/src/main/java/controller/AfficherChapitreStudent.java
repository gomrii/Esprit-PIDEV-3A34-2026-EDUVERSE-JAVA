package controller;

import Entities.Chapitre;
import Entities.Cours;
import Services.ServiceChapitre;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class AfficherChapitreStudent {

    @FXML private VBox chaptersListContainer;
    @FXML private Label contentChapterTitle, chapterContentArea;
    @FXML private HBox mediaContainer;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private Cours currentCours;
    private Chapitre selectedChapter;

    // --- CETTE MÉTHODE EST LA CLÉ DE L'AFFICHAGE ---
    public void setCoursData(Cours cours) {
        this.currentCours = cours;
        System.out.println("Cours reçu avec succès : " + cours.getTitle());
        loadChapters(); // On lance le chargement dès qu'on a le cours
    }

    private void loadChapters() {
        if (currentCours == null) return;

        try {
            chaptersListContainer.getChildren().clear();
            List<Chapitre> chapitres = serviceChapitre.getChapitresByCoursId(currentCours.getId());

            System.out.println("Nombre de chapitres trouvés en BDD : " + chapitres.size());

            for (Chapitre ch : chapitres) {
                Button btn = new Button("📖 " + ch.getTitle());
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle("-fx-background-color: white; -fx-border-color: #F1F2F6; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
                btn.setOnAction(e -> showLesson(ch));
                chaptersListContainer.getChildren().add(btn);
            }

            if (!chapitres.isEmpty()) {
                showLesson(chapitres.get(0));
            } else {
                contentChapterTitle.setText("Aucun chapitre trouvé");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showLesson(Chapitre ch) {
        this.selectedChapter = ch; // Crucial pour le bouton supprimer/modifier

        contentChapterTitle.setText(ch.getTitle());
        chapterContentArea.setText(ch.getContenu()); // Envoie le texte vers le Label



        mediaContainer.getChildren().clear();
    }

    @FXML
    private void handleGeneratePDF(ActionEvent event) {
        if (selectedChapter == null) {
            showAlert("Erreur", "Veuillez sélectionner un chapitre.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(selectedChapter.getTitle().replaceAll(" ", "_") + ".pdf");
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            // Utilisation explicite pour éviter les conflits d'imports
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Design du PDF
                com.itextpdf.text.Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
                document.add(new Paragraph(selectedChapter.getTitle(), fTitle));
                document.add(new Chunk(new LineSeparator()));
                document.add(new Paragraph("\n" + selectedChapter.getContenu()));

                document.close();
                showAlert("Succès", "PDF généré !", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(content);
        a.show();
    }

    @FXML
    public void handleBack(ActionEvent actionEvent) {
        try {
            // Chargement du fichier FXML de la vue étudiant
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierCoursStudent.fxml"));
            Parent root = loader.load();

            // Récupération de la scène actuelle à partir de l'événement
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Changement de la scène
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            // Affichage d'une erreur si le fichier FXML est introuvable
            System.err.println("Erreur de navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}