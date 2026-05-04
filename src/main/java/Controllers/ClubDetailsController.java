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
import java.util.List;

public class ClubDetailsController {

    @FXML private Label clubNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label creatorLabel;
    @FXML private Label statusLabel;
    @FXML private ListView<String> membersListView;
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
        try {
            List<String> members = serviceClub.getClubMembers(clubId);
            if (members.isEmpty()) {
                membersListView.getItems().add("Aucun membre pour le moment.");
            } else {
                membersListView.getItems().addAll(members);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            membersListView.getItems().add("Erreur lors du chargement des membres.");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherClub.fxml", "Gestion des Clubs");
    }

    @FXML
    private void handleJoin(ActionEvent event) {
        if (currentClub == null) return;
        try {
            serviceClub.requestJoinClub(1, currentClub.getId());
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
