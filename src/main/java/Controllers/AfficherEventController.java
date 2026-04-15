package Controllers;

import Entities.Event;
import Services.ServiceEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class AfficherEventController {

    @FXML private TextField tfSearch;
    @FXML private TableView<Event> tvEvents;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, Date> colDate;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, String> colStatus;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private ObservableList<Event> eventList;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Désactiver le tri pour tout sauf le Nom (Titre)
        colDate.setSortable(false);
        colLocation.setSortable(false);
        colStatus.setSortable(false);

        loadEvents();
    }

    private void loadEvents() {
        try {
            eventList = FXCollections.observableArrayList(serviceEvent.display());
            
            // 1. Initialiser le Filtre (FilteredList)
            FilteredList<Event> filteredData = new FilteredList<>(eventList, b -> true);

            // 2. Écouter les changements de texte pour la recherche temps réel
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(event -> {
                    // Si le champ est vide, afficher tout
                    if (newValue == null || newValue.isEmpty()) return true;
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    
                    // Recherche STRICTEMENT sur le nom/titre (Premiers caractères exclusifs) :
                    return event.getTitle().toLowerCase().startsWith(lowerCaseFilter);
                });
            });

            // 3. Lier au tri dynamique (SortedList)
            SortedList<Event> sortedData = new SortedList<>(filteredData);
            
            // Lier le tri de la TableView à notre SortedList
            sortedData.comparatorProperty().bind(tvEvents.comparatorProperty());

            // 4. Mettre les données dans la TableView
            tvEvents.setItems(sortedData);

            // Appliquer un tri par défaut sur la colonne Titre/Nom
            tvEvents.getSortOrder().add(colTitle);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isAscending = true;

    @FXML
    private void trierAZ() {
        tvEvents.getSortOrder().clear();
        if (isAscending) {
            colTitle.setSortType(TableColumn.SortType.DESCENDING);
            isAscending = false;
        } else {
            colTitle.setSortType(TableColumn.SortType.ASCENDING);
            isAscending = true;
        }
        tvEvents.getSortOrder().add(colTitle);
        tvEvents.sort();
    }

    @FXML
    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Liste_Evenements.pdf");

        File file = fileChooser.showSaveDialog(tvEvents.getScene().getWindow());
        if (file == null) {
            return; // Annulé par l'utilisateur
        }

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            document.add(new Paragraph("Liste des Événements\n\n"));

            PdfPTable table = new PdfPTable(4);
            table.addCell("Titre");
            table.addCell("Date");
            table.addCell("Lieu");
            table.addCell("Statut");

            // On boucle sur tvEvents.getItems() pour exporter exactement ce qu'on voit à l'écran (avec filtres !)
            for (Event event : tvEvents.getItems()) {
                table.addCell(event.getTitle());
                table.addCell(event.getEventDate() != null ? event.getEventDate().toString() : "");
                table.addCell(event.getLocation());
                table.addCell(event.getStatus());
            }

            document.add(table);
            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF généré avec succès !\nFichier : " + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EventMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
