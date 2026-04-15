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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ModifierClubController {

    @FXML private TableView<Club> tvClubs;
    @FXML private TableColumn<Club, Integer> colId;
    @FXML private TableColumn<Club, String> colName;
    
    @FXML private VBox formModifier;
    @FXML private TextField tfName;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbStatus;

    private final ServiceClub serviceClub = new ServiceClub();
    private int selectedClubId = -1;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE", "PENDING"));

        formModifier.setDisable(true);
        loadClubs();

        tvClubs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });
    }

    private void loadClubs() {
        try {
            ObservableList<Club> clubList = FXCollections.observableArrayList(serviceClub.display());
            tvClubs.setItems(clubList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillForm(Club club) {
        formModifier.setDisable(false);
        selectedClubId = club.getId();
        tfName.setText(club.getName());
        taDescription.setText(club.getDescription());
        cbStatus.setValue(club.getStatus());
    }

    @FXML
    private void updateClub(ActionEvent ev) {
        if (selectedClubId == -1) return;

        try {
            Club club = new Club();
            club.setId(selectedClubId);
            club.setName(tfName.getText().trim());
            club.setDescription(taDescription.getText().trim());
            club.setStatus(cbStatus.getValue());

            serviceClub.update(club);
            
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Club modifié !");
            a.showAndWait();
            
            formModifier.setDisable(true);
            selectedClubId = -1;
            loadClubs();

        } catch (SQLException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Erreur SQL: " + e.getMessage());
            a.showAndWait();
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
