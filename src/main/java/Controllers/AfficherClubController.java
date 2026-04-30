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
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherClubController {

    @FXML private FlowPane clubsContainer;
    private final ServiceClub serviceClub = new ServiceClub();

    @FXML
    public void initialize() {
        loadClubs();
    }

    private void loadClubs() {
        if (Utils.MyDb.getInstance().getConn() == null) {
            clubsContainer.getChildren().clear();
            Label errorLabel = new Label("⚠️ Erreur : Impossible de se connecter à la base de données.");
            errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-font-weight: bold;");
            clubsContainer.getChildren().add(errorLabel);
            return;
        }
        try {
            List<Club> clubList = serviceClub.display();
            clubsContainer.getChildren().clear();

            for (Club club : clubList) {
                VBox card = new VBox(10);
                card.getStyleClass().addAll("stat-card", "card-blue");
                card.setPrefWidth(220);
                
                Label nameLabel = new Label(club.getName());
                nameLabel.getStyleClass().add("card-title");
                nameLabel.setWrapText(true);

                Label descLabel = new Label(club.getDescription());
                descLabel.getStyleClass().add("card-desc");
                descLabel.setWrapText(true);
                descLabel.setMaxHeight(60);

                Label statusLabel = new Label("Statut: " + club.getStatus());
                statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 12px;");

                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);
                actions.setPadding(new Insets(10, 0, 0, 0));
                
                Button btnEdit = new Button("✏");
                btnEdit.getStyleClass().add("btn-modifier");
                btnEdit.setOnAction(e -> modifierClub(club, e));
                
                Button btnDelete = new Button("🗑");
                btnDelete.getStyleClass().add("btn-supprimer");
                btnDelete.setOnAction(e -> supprimerClub(club, e));
                
                actions.getChildren().addAll(btnEdit, btnDelete);

                card.getChildren().addAll(nameLabel, descLabel, statusLabel, actions);
                clubsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
