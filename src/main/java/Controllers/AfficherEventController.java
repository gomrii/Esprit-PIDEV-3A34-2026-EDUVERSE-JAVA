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
import javafx.scene.layout.HBox;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AfficherEventController {

    @FXML private TextField tfSearch;
    @FXML private TableView<Event> tvEvents;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, Date> colDate;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, String> colStatus;
    @FXML private TableColumn<Event, Void> colActions;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private ObservableList<Event> eventList;
    
    // Set statique pour ne pas répéter l'alerte même si on recharge la vue
    private static final Set<String> alertedEvents = new HashSet<>();

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionsColumn();

        // Désactiver le tri pour tout sauf le Nom (Titre)
        colDate.setSortable(false);
        colLocation.setSortable(false);
        colStatus.setSortable(false);
        colActions.setSortable(false);

        loadEvents();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");
            private final HBox container = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-modifier");
                btnDelete.getStyleClass().add("btn-supprimer");
                btnEdit.setOnAction(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    modifierEvent(e);
                });
                btnDelete.setOnAction(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    supprimerEvent(e);
                });
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void modifierEvent(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
            Parent root = loader.load();
            ModifierEventController controller = loader.getController();
            controller.setEvent(event);
            
            MainDashboardController.getInstance().loadViewFromParent(root, "Modifier l'Événement");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer l'événement '" + event.getTitle() + "' ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEvent.delete(event);
                    loadEvents();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setContentText("Impossible de supprimer l'événement : " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }

    private void loadEvents() {
        if (Utils.MyDb.getInstance().getConn() == null) {
            tvEvents.setPlaceholder(new Label("⚠️ Impossible de se connecter à la base de données."));
            return;
        }
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

            // Vérification des événements proches (Bonus & Intégration)
            verifierEvenementsProches(eventList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logique métier : Vérifie si un événement est prévu dans 2 jours ou moins
     * (Bonus : Utilisation de LocalDate et ChronoUnit)
     */
    private boolean isEventProche(Date eventDate) {
        if (eventDate == null) return false;
        
        LocalDate today = LocalDate.now();
        LocalDate eDate;
        
        // Correction de l'erreur : java.sql.Date ne supporte pas toInstant()
        if (eventDate instanceof java.sql.Date) {
            eDate = ((java.sql.Date) eventDate).toLocalDate();
        } else {
            // Solution robuste : convertir n'importe quel java.util.Date en java.sql.Date d'abord
            eDate = new java.sql.Date(eventDate.getTime()).toLocalDate();
        }
        
        long daysBetween = ChronoUnit.DAYS.between(today, eDate);
        
        // si (eventDate - today <= 2 jours) ET eventDate >= today
        return daysBetween >= 0 && daysBetween <= 2;
    }

    /**
     * Parcourt la liste pour afficher l'alerte si nécessaire
     */
    private void verifierEvenementsProches(ObservableList<Event> events) {
        javafx.application.Platform.runLater(() -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
            
            for (Event event : events) {
                try {
                    if (isEventProche(event.getEventDate()) && !alertedEvents.contains(event.getTitle())) {
                        
                        // Ajout dans le Set pour éviter la répétition
                        alertedEvents.add(event.getTitle());
                        
                        // Récupération de la LocalDate pour le formatage
                        LocalDate eDate;
                        if (event.getEventDate() instanceof java.sql.Date) {
                            eDate = ((java.sql.Date) event.getEventDate()).toLocalDate();
                        } else {
                            eDate = new java.sql.Date(event.getEventDate().getTime()).toLocalDate();
                        }
                        
                        String formattedDate = eDate.format(formatter);
                        
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Événement Proche");
                        alert.setHeaderText("Rappel d'événement");
                        alert.setContentText("L'événement " + event.getTitle() + " prévu le " + formattedDate + " approche (dans moins de 2 jours).");
                        alert.showAndWait();
                    }
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur de traitement");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Erreur lors du traitement de la date de l'événement " + event.getTitle() + ".");
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
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
    private void goAjouterEvent(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AjouterEvent.fxml", "Ajouter un Événement");
    }

    @FXML
    private void goCalendar(ActionEvent event) {
        MainDashboardController.getInstance().loadView("EventCalendar.fxml", "Calendrier des Événements");
    }

    @FXML
    private void goBackHome(ActionEvent event) {
        MainDashboardController.getInstance().loadView("Home.fxml", "Tableau de Bord");
    }
}
