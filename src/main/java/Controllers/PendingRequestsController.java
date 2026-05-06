package Controllers;

import Entities.Event;
import Entities.Club;
import Services.ServiceEvent;
import Services.ServiceClub;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PendingRequestsController {

    // Éléments Événements
    @FXML private TableView<Event> tvPendingEvents;
    @FXML private TableColumn<Event, String> colEventTitle;
    @FXML private TableColumn<Event, Date> colEventDate;
    @FXML private TableColumn<Event, Void> colEventActions;

    // Éléments Clubs
    @FXML private TableView<Club> tvPendingClubs;
    @FXML private TableColumn<Club, String> colClubName;
    @FXML private TableColumn<Club, Void> colClubActions;

    // Éléments Adhésions
    @FXML private TableView<String[]> tvJoinRequests;
    @FXML private TableColumn<String[], String> colUserReq;
    @FXML private TableColumn<String[], String> colClubReq;
    @FXML private TableColumn<String[], Void> colJoinActions;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private final ServiceClub serviceClub = new ServiceClub();

    @FXML
    public void initialize() {
        // Init Événements
        colEventTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colEventDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        setupEventActionsColumn();

        // Init Clubs
        colClubName.setCellValueFactory(new PropertyValueFactory<>("name"));
        setupClubActionsColumn();

        // Init Adhésions
        colUserReq.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colClubReq.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        setupJoinActionsColumn();

        loadData();
    }

    private void setupEventActionsColumn() {
        colEventActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnApprove = new Button("✅ Approuver");
            private final Button btnReject = new Button("❌ Refuser");
            private final HBox container = new HBox(10, btnApprove, btnReject);
            {
                btnApprove.getStyleClass().add("btn-success");
                btnReject.getStyleClass().add("btn-danger");
                btnApprove.setOnAction(e -> handleEventDecision(getTableView().getItems().get(getIndex()), "APPROVED"));
                btnReject.setOnAction(e -> handleEventDecision(getTableView().getItems().get(getIndex()), "REJECTED"));
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupClubActionsColumn() {
        colClubActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnApprove = new Button("✅ Valider");
            private final Button btnReject = new Button("❌ Refuser");
            private final HBox container = new HBox(10, btnApprove, btnReject);
            {
                btnApprove.getStyleClass().add("btn-success");
                btnReject.getStyleClass().add("btn-danger");
                btnApprove.setOnAction(e -> handleClubDecision(getTableView().getItems().get(getIndex()), "APPROVED"));
                btnReject.setOnAction(e -> handleClubDecision(getTableView().getItems().get(getIndex()), "REJECTED"));
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupJoinActionsColumn() {
        colJoinActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnApprove = new Button("✅ Accepter");
            private final Button btnReject = new Button("❌ Refuser");
            private final HBox container = new HBox(10, btnApprove, btnReject);
            {
                btnApprove.getStyleClass().add("btn-success");
                btnReject.getStyleClass().add("btn-danger");
                btnApprove.setOnAction(e -> handleJoinDecision(getTableView().getItems().get(getIndex()), "APPROVED"));
                btnReject.setOnAction(e -> handleJoinDecision(getTableView().getItems().get(getIndex()), "REJECTED"));
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void handleEventDecision(Event event, String status) {
        try {
            serviceEvent.updateStatus(event.getId(), status);
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void handleClubDecision(Club club, String status) {
        try {
            serviceClub.validateClub(club.getId(), status);
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void handleJoinDecision(String[] request, String status) {
        try {
            int userId = Integer.parseInt(request[0]);
            int clubId = Integer.parseInt(request[1]);
            serviceClub.updateMembershipStatus(userId, clubId, status);
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadData() {
        try {
            tvPendingEvents.setItems(FXCollections.observableArrayList(serviceEvent.getPendingEvents()));
            tvPendingClubs.setItems(FXCollections.observableArrayList(serviceClub.getPendingClubs()));
            tvJoinRequests.setItems(FXCollections.observableArrayList(serviceClub.getPendingJoinRequests()));
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
