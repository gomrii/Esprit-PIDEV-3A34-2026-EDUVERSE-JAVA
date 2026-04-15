package Controllers;

import Entities.Club;
import Services.ServiceClub;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SupprimerClubController {

    @FXML private TableView<Club> tvClubs;
    @FXML private TableColumn<Club, Integer> colId;
    @FXML private TableColumn<Club, String> colName;

    private final ServiceClub serviceClub = new ServiceClub();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        loadClubs();
    }

    private void loadClubs() {
        try {
            ObservableList<Club> clubList = FXCollections.observableArrayList(serviceClub.display());
            tvClubs.setItems(clubList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteClub() {
        Club selectedClub = tvClubs.getSelectionModel().getSelectedItem();
        if (selectedClub == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un club.");
            a.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    serviceClub.delete(selectedClub);
                    loadClubs(); 
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Club supprimé !");
                    a.showAndWait();
                } catch (SQLException e) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Erreur SQL: " + e.getMessage());
                    a.showAndWait();
                }
            }
        });
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ClubMenu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
