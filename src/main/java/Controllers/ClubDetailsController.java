package Controllers;

import Entities.Club;
import Services.ServiceClub;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import Utils.Session;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import Utils.MyDb;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Insets;

public class ClubDetailsController {

    @FXML private Label clubNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label creatorLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane membersFlowPane; // Remplace ListView si possible, sinon on utilise le FlowPane dynamiquement
    @FXML private ListView<Label> membersListView; // Mise à jour pour afficher des Labels (Chips)
    @FXML private Button btnJoin;

    private final ServiceClub serviceClub = new ServiceClub();
    private Club currentClub;

    public void setClub(Club club) {
        this.currentClub = club;
        clubNameLabel.setText(club.getName());
        descriptionLabel.setText(club.getDescription());
        creatorLabel.setText("ID du créateur : " + club.getCreatorId());
        statusLabel.setText(club.getStatus());

        // Visibilité du bouton rejoindre (uniquement pour non-admins)
        if (btnJoin != null) {
            btnJoin.setVisible(!"ADMIN".equals(Session.role));
        }

        loadMembers(club.getId());
    }

    private void loadMembers(int clubId) {
        membersListView.getItems().clear();
        try {
            // 1. Récupérer uniquement les user_id (sans JOIN)
            List<Integer> userIds = serviceClub.getClubMemberIds(clubId);
            
            if (userIds.isEmpty()) {
                membersListView.getItems().add(new Label("Aucun membre pour le moment."));
                return;
            }
            
            // 2. Pour chaque user_id, on récupère son nom et rôle, puis on crée un "Chip"
            for (Integer userId : userIds) {
                String texteMembre = getNomMembre(userId);
                
                Label chipLabel = new Label(texteMembre);
                chipLabel.setPadding(new Insets(5, 12, 5, 12));
                
                // Style Chips UI en fonction du rôle
                if (texteMembre.contains("ADMIN")) {
                    chipLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 15px; -fx-font-weight: bold;");
                } else if (texteMembre.contains("ENSEIGNANT")) {
                    chipLabel.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 15px; -fx-font-weight: bold;");
                } else {
                    chipLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15px; -fx-font-weight: bold;");
                }
                
                membersListView.getItems().add(chipLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            membersListView.getItems().add(new Label("Erreur lors du chargement des membres."));
        }
    }

    private String getNomMembre(int userId) {
        String resultat = "Inconnu";
        String query = "SELECT full_name, role FROM user WHERE id = ?"; 
        
        try (Connection cnx = MyDb.getInstance().getConn(); 
             PreparedStatement pst = cnx.prepareStatement(query)) {
             
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("full_name");
                String role = rs.getString("role");
                System.out.println("Membre ID " + userId + " -> " + name);
                resultat = name + " (" + role + ")";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultat;
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    @FXML
    private void handleJoin(ActionEvent event) {
        if (currentClub == null) return;
        try {
            // Remplacement du 1 codé en dur par l'ID de l'utilisateur connecté
            serviceClub.requestJoinClub(Session.userId, currentClub.getId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Votre demande d'adhésion a été envoyée !");
            alert.showAndWait();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'envoyer la demande (vous avez peut-être déjà postulé).");
            alert.showAndWait();
        }
    }
}
