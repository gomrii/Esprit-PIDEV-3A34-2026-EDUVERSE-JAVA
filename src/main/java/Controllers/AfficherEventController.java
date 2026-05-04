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
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

public class AfficherEventController {

    @FXML private TextField tfSearch;
    @FXML private FlowPane eventsContainer;
    @FXML private FlowPane myEventsContainer;
    @FXML private TabPane tabPane;
    @FXML private Tab tabMyEvents;
    @FXML private Button btnAjouterEvent;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private List<Event> allEventsList = new ArrayList<>();
    
    // Set statique pour ne pas répéter l'alerte même si on recharge la vue
    private static final Set<String> alertedEvents = new HashSet<>();

    @FXML
    public void initialize() {
        // --- GESTION DES PERMISSIONS (Bouton Ajouter) ---
        if (btnAjouterEvent != null) {
            btnAjouterEvent.setVisible(true);
        }

        loadEvents();

        // Recherche en temps réel
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEvents(newValue);
        });
    }

    private void loadEvents() {
        try {
            if ("ADMIN".equals(Utils.Session.role)) {
                allEventsList = serviceEvent.display();
            } else {
                List<Event> approved = serviceEvent.displayApproved();
                List<Event> myEvents = serviceEvent.getEventsByCreator(Utils.Session.userId);
                
                Set<Integer> ids = new HashSet<>();
                allEventsList = new ArrayList<>();
                
                for (Event e : approved) {
                    allEventsList.add(e);
                    ids.add(e.getId());
                }
                for (Event e : myEvents) {
                    if (!ids.contains(e.getId())) {
                        allEventsList.add(e);
                    }
                }
            }
            
            renderCards(allEventsList);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderCards(List<Event> events) {
        eventsContainer.getChildren().clear();
        myEventsContainer.getChildren().clear();
        
        int currentUserId = Utils.Session.userId;
        
        for (Event event : events) {
            VBox card = createEventCard(event);
            
            // Distribution dans les conteneurs
            if (event.getCreatorId() == currentUserId) {
                myEventsContainer.getChildren().add(card);
            } else if ("APPROVED".equals(event.getStatus())) {
                eventsContainer.getChildren().add(card);
            } else if ("ADMIN".equals(Utils.Session.role)) {
                // L'admin voit tout dans l'onglet principal
                eventsContainer.getChildren().add(card);
            }
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-cursor: hand;");

        Label lblTitle = new Label(event.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        lblTitle.setWrapText(true);

        Label lblDetails = new Label("📅 " + event.getEventDate().toString() + "\n📍 " + event.getLocation());
        lblDetails.setWrapText(true);
        lblDetails.setStyle("-fx-text-fill: #666;");

        Label statusLabel = new Label(event.getStatus());
        statusLabel.getStyleClass().add("status-badge");
        if ("APPROVED".equals(event.getStatus())) {
            statusLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
        } else if ("REJECTED".equals(event.getStatus())) {
            statusLabel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
        } else {
            statusLabel.setStyle("-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // --- GESTION DES PERMISSIONS ---
        if ("ADMIN".equals(Utils.Session.role)) {
            // Boutons de validation rapide pour l'admin
            if (!"APPROVED".equals(event.getStatus())) {
                Button btnApprove = new Button("✅");
                btnApprove.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand;");
                btnApprove.setOnAction(e -> changerStatutEvent(event, "APPROVED"));
                actions.getChildren().add(btnApprove);
            }
            if (!"REJECTED".equals(event.getStatus())) {
                Button btnReject = new Button("❌");
                btnReject.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");
                btnReject.setOnAction(e -> changerStatutEvent(event, "REJECTED"));
                actions.getChildren().add(btnReject);
            }
            
            Button btnEdit = new Button("✏");
            btnEdit.getStyleClass().add("btn-modifier");
            btnEdit.setOnAction(e -> modifierEvent(event));

            Button btnDelete = new Button("🗑");
            btnDelete.getStyleClass().add("btn-supprimer");
            btnDelete.setOnAction(e -> supprimerEvent(event));

            actions.getChildren().addAll(btnEdit, btnDelete);
        } else if (event.getCreatorId() == Utils.Session.userId && "PENDING".equals(event.getStatus())) {
            Button btnEdit = new Button("✏");
            btnEdit.getStyleClass().add("btn-modifier");
            btnEdit.setOnAction(e -> modifierEvent(event));

            Button btnDelete = new Button("🗑");
            btnDelete.getStyleClass().add("btn-supprimer");
            btnDelete.setOnAction(e -> supprimerEvent(event));

            actions.getChildren().addAll(btnEdit, btnDelete);
        }

        card.getChildren().addAll(lblTitle, lblDetails, statusLabel, spacer, actions);

        // --- CLIC POUR DÉTAILS ---
        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button || (e.getTarget() instanceof javafx.scene.text.Text && ((javafx.scene.text.Text)e.getTarget()).getParent() instanceof Button)) {
                return;
            }
            showDetails(event);
        });

        return card;
    }

    private void changerStatutEvent(Event event, String nouveauStatut) {
        try {
            serviceEvent.updateStatus(event.getId(), nouveauStatut);
            loadEvents();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors du changement de statut: " + e.getMessage()).showAndWait();
        }
    }

    private void filterEvents(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            renderCards(allEventsList);
            return;
        }
        
        List<Event> filtered = allEventsList.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(searchText.toLowerCase()) || 
                             e.getLocation().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
        
        renderCards(filtered);
    }

    private void modifierEvent(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
            Parent root = loader.load();
            ModifierEventController controller = loader.getController();
            controller.setEvent(event);
            MainDashboardController.getInstance().loadViewFromParent(root, "Modifier l'Événement");
        } catch (IOException e) { e.printStackTrace(); }
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
                    new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + ex.getMessage()).showAndWait();
                }
            }
        });
    }

    private void verifierEvenementsProches(List<Event> events) {
        javafx.application.Platform.runLater(() -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
            for (Event event : events) {
                try {
                    if (isEventProche(event.getEventDate()) && !alertedEvents.contains(event.getTitle())) {
                        alertedEvents.add(event.getTitle());
                        LocalDate eDate = (event.getEventDate() instanceof java.sql.Date) ? 
                            ((java.sql.Date) event.getEventDate()).toLocalDate() : 
                            new java.sql.Date(event.getEventDate().getTime()).toLocalDate();
                        
                        String formattedDate = eDate.format(formatter);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Événement Proche");
                        alert.setHeaderText("Rappel d'événement");
                        alert.setContentText("L'événement " + event.getTitle() + " prévu le " + formattedDate + " approche (dans moins de 2 jours).");
                        alert.showAndWait();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private boolean isEventProche(Date eventDate) {
        if (eventDate == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate eDate = (eventDate instanceof java.sql.Date) ? 
            ((java.sql.Date) eventDate).toLocalDate() : 
            new java.sql.Date(eventDate.getTime()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(today, eDate);
        return daysBetween >= 0 && daysBetween <= 2;
    }

    @FXML
    void trierAZ(ActionEvent event) {
        allEventsList.sort((e1, e2) -> e1.getTitle().compareToIgnoreCase(e2.getTitle()));
        filterEvents(tfSearch.getText());
    }

    @FXML
    private void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Liste_Evenements.pdf");

        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        if (file == null) return;

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

            for (Event e : allEventsList) {
                table.addCell(e.getTitle());
                table.addCell(e.getEventDate() != null ? e.getEventDate().toString() : "");
                table.addCell(e.getLocation());
                table.addCell(e.getStatus());
            }

            document.add(table);
            document.close();
            new Alert(Alert.AlertType.INFORMATION, "PDF généré avec succès !").showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur PDF: " + e.getMessage()).showAndWait();
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

    private void showDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();
            EventDetailsController controller = loader.getController();
            controller.setEvent(event);
            MainDashboardController.getInstance().loadViewFromParent(root, "Détails de l'Événement");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
