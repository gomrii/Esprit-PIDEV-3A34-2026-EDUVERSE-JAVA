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

public class AfficherClubController {

    @FXML private TableView<Club> tvClubs;
    @FXML private TableColumn<Club, Integer> colId;
    @FXML private TableColumn<Club, String> colName;
    @FXML private TableColumn<Club, String> colDescription;
    @FXML private TableColumn<Club, String> colStatus;

    private final ServiceClub serviceClub = new ServiceClub();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

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
