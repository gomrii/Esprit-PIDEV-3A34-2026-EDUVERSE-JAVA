package Controllers;

import Entities.Club;
import Services.ServiceClub;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import Utils.Session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherClubController {

    @FXML private TextField tfSearch;
    @FXML private FlowPane clubsContainer;
    @FXML private FlowPane myClubsContainer;
    @FXML private TabPane tabPane;
    @FXML private Tab tabMyClubs;
    @FXML private Button btnAjouterClub;

    private final ServiceClub serviceClub = new ServiceClub();
    private List<Club> allClubsList = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        // Tout le monde peut ajouter
        if (btnAjouterClub != null) {
            btnAjouterClub.setVisible(true);
        }
        
        // Cacher l'onglet "Mes Clubs" pour l'Admin car il gère tout via l'onglet principal
        if ("ADMIN".equals(Session.role)) {
            tabPane.getTabs().remove(tabMyClubs);
        }
        
        loadClubs();

        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> filterClubs(newVal));
        }
    }

    private void loadClubs() {
        try {
            if ("ADMIN".equals(Session.role)) {
                allClubsList = serviceClub.display();
            } else {
                List<Club> approved = serviceClub.displayApproved();
                List<Club> myClubs = serviceClub.getClubsByCreator(Session.userId);
                
                java.util.Set<Integer> ids = new java.util.HashSet<>();
                allClubsList = new java.util.ArrayList<>();
                
                for (Club c : approved) {
                    allClubsList.add(c);
                    ids.add(c.getId());
                }
                for (Club c : myClubs) {
                    if (!ids.contains(c.getId())) {
                        allClubsList.add(c);
                    }
                }
            }
            renderCards(allClubsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterClubs(String query) {
        if (query == null || query.isEmpty()) {
            renderCards(allClubsList);
            return;
        }
        List<Club> filtered = allClubsList.stream()
                .filter(c -> c.getName().toLowerCase().contains(query.toLowerCase()) || 
                             c.getDescription().toLowerCase().contains(query.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
        renderCards(filtered);
    }

    private void renderCards(List<Club> clubs) {
        clubsContainer.getChildren().clear();
        if (myClubsContainer != null) myClubsContainer.getChildren().clear();
        
        int currentUserId = Session.userId;
        
        for (Club club : clubs) {
            VBox card = createClubCard(club, currentUserId);
            
            // Distribution
            if ("ADMIN".equals(Session.role)) {
                clubsContainer.getChildren().add(card);
            } else {
                if (club.getCreatorId() == currentUserId) {
                    myClubsContainer.getChildren().add(card);
                } else if ("APPROVED".equals(club.getStatus())) {
                    clubsContainer.getChildren().add(card);
                }
            }
        }
    }

    private VBox createClubCard(Club club, int currentUserId) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label nameLabel = new Label(club.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        
        Label descLabel = new Label(club.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #666;");

        Label statusLabel = new Label(club.getStatus());
        statusLabel.getStyleClass().add("status-badge");
        if ("APPROVED".equals(club.getStatus())) {
            statusLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
        } else if ("REJECTED".equals(club.getStatus())) {
            statusLabel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
        } else {
            statusLabel.setStyle("-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
        }

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("btn-modifier");
        btnEdit.setOnAction(e -> modifierClub(club, e));

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().add("btn-supprimer");
        btnDelete.setOnAction(e -> supprimerClub(club, e));

        // --- GESTION DES PERMISSIONS & BOUTONS D'ACTION ---
        if ("ADMIN".equals(Session.role)) {
            actions.getChildren().addAll(btnEdit, btnDelete);
            if ("APPROVED".equals(club.getStatus())) {
                Button btnToReject = new Button("❌");
                btnToReject.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");
                btnToReject.setOnAction(e -> changerStatutClub(club, "REJECTED"));
                actions.getChildren().add(0, btnToReject);
            } else if ("REJECTED".equals(club.getStatus())) {
                Button btnToApprove = new Button("✅");
                btnToApprove.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand;");
                btnToApprove.setOnAction(e -> changerStatutClub(club, "APPROVED"));
                actions.getChildren().add(0, btnToApprove);
            } else if ("PENDING".equals(club.getStatus())) {
                Button btnToApprove = new Button("✅");
                btnToApprove.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand;");
                btnToApprove.setOnAction(e -> changerStatutClub(club, "APPROVED"));
                
                Button btnToReject = new Button("❌");
                btnToReject.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");
                btnToReject.setOnAction(e -> changerStatutClub(club, "REJECTED"));
                
                actions.getChildren().addAll(0, List.of(btnToApprove, btnToReject));
            }
        } else if (club.getCreatorId() == currentUserId) {
            // Un créateur peut modifier/supprimer son club TANT QU'IL est PENDING (selon l'énoncé)
            if ("PENDING".equals(club.getStatus())) {
                actions.getChildren().addAll(btnEdit, btnDelete);
            } else {
                Label infoLabel = new Label("Validé");
                infoLabel.setStyle("-fx-text-fill: #10b981; -fx-font-style: italic;");
                actions.getChildren().add(infoLabel);
            }
        } else {
            Button btnJoin = new Button("🤝 Rejoindre");
            btnJoin.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
            btnJoin.setOnAction(e -> rejoindreClub(club));
            actions.getChildren().add(btnJoin);
        }

        card.getChildren().addAll(nameLabel, descLabel, statusLabel, actions);

        // --- CLIC POUR DÉTAILS ---
        card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button || (e.getTarget() instanceof javafx.scene.text.Text && ((javafx.scene.text.Text)e.getTarget()).getParent() instanceof Button)) {
                return;
            }
            showDetails(club);
        });

        return card;
    }

    @FXML
    private void goAjouterClub(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AjouterClub.fxml", "Ajouter un Club");
    }

    private void modifierClub(Club club, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierClub.fxml"));
            Parent root = loader.load();
            ModifierClubController controller = loader.getController();
            controller.setClub(club);
            
            // On charge manuellement dans le dashboard
            MainDashboardController.getInstance().loadViewFromParent(root, "Modifier le Club");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerClub(Club club, ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer le club '" + club.getName() + "' ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceClub.delete(club);
                    loadClubs();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setContentText("Impossible de supprimer le club : " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }
    @FXML
    private void goBackHome(ActionEvent event) {
        MainDashboardController.getInstance().loadView("Home.fxml", "Tableau de Bord");
    }

    private void changerStatutClub(Club club, String nouveauStatut) {
        try {
            serviceClub.validateClub(club.getId(), nouveauStatut);
            loadClubs(); // Recharger la liste
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDetails(Club club) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClubDetails.fxml"));
            Parent root = loader.load();
            ClubDetailsController controller = loader.getController();
            controller.setClub(club);
            
            MainDashboardController.getInstance().loadViewFromParent(root, "Détails du Club");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rejoindreClub(Club club) {
        try {
            serviceClub.requestJoinClub(Session.userId, club.getId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Demande envoyée");
            alert.setHeaderText(null);
            alert.setContentText("Votre demande pour rejoindre le club '" + club.getName() + "' a été envoyée à l'administrateur.");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Vous avez peut-être déjà envoyé une demande pour ce club.");
            alert.showAndWait();
        }
    }
}
