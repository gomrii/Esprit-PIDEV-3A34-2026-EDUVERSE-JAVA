package edu.connexion3a36.Controller;

import edu.connexion3a36.entities.Reponse;
import edu.connexion3a36.services.ReponseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;

public class ListeReponseController {

    @FXML
    private TableView<Reponse> reponsesTable;

    @FXML
    private TableColumn<Reponse, Integer> colId;

    @FXML
    private TableColumn<Reponse, String> colReponse;

    @FXML
    private TableColumn<Reponse, Double> colScore;

    @FXML
    private TableColumn<Reponse, Integer> colIdQuestion;

    @FXML
    private TextField searchTF;

    @FXML
    private ChoiceBox<String> sortChoice;

    private final ReponseService reponseService = new ReponseService();
    private ObservableList<Reponse> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReponse"));
        colReponse.setCellValueFactory(new PropertyValueFactory<>("reponse"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colIdQuestion.setCellValueFactory(new PropertyValueFactory<>("idQuestion"));
        sortChoice.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID décroissant",
                "Réponse A-Z",
                "Réponse Z-A",
                "Score croissant",
                "Score décroissant"
        ));
        sortChoice.setValue("ID croissant");
        sortChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        chargerReponses();
    }

    public void chargerReponses() {
        try {
            masterData = FXCollections.observableArrayList(reponseService.afficherReponse());
            applyFiltersAndSort();
        } catch (SQLException e) {
            ControllerUtils.showError("Erreur lors du chargement des réponses : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFiltersAndSort();
    }

    public Reponse getSelectedReponse() {
        return reponsesTable.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        openFXML(event, "/answer_add.fxml");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (getSelectedReponse() == null) {
            ControllerUtils.showWarning("Sélectionnez une réponse à modifier.");
            return;
        }
        openFXML(event, "/answer_edit.fxml");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        supprimerReponse(event);
    }

    @FXML
    private void supprimerReponse(ActionEvent event) {
        if (getSelectedReponse() == null) {
            ControllerUtils.showWarning("Sélectionnez une réponse à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la réponse sélectionnée ?", javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == javafx.scene.control.ButtonType.YES) {
            try {
                reponseService.supprimerReponse(getSelectedReponse().getIdReponse());
                ControllerUtils.showInfo("Réponse supprimée.");
                chargerReponses();
            } catch (SQLException e) {
                ControllerUtils.showError("Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchTF != null) {
            searchTF.clear();
        }
        chargerReponses();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent root = loader.load();
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }

    private void openFXML(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            try {
                if (controller != null) {
                    java.lang.reflect.Method m = null;
                    try {
                        m = controller.getClass().getMethod("setReponse", Reponse.class);
                    } catch (NoSuchMethodException ignored) {
                    }
                    if (m != null) {
                        m.invoke(controller, getSelectedReponse());
                    }
                }
            } catch (Exception ignored) {
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) {
                Scene newScene = new Scene(root, 1100, 700);
                newScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(newScene);
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            ControllerUtils.showError("Impossible d'ouvrir la page: " + e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        String q = searchTF == null || searchTF.getText() == null ? "" : searchTF.getText().trim().toLowerCase();
        ObservableList<Reponse> filtered = FXCollections.observableArrayList();
        for (Reponse item : masterData) {
            boolean matches = q.isEmpty()
                    || String.valueOf(item.getIdReponse()).contains(q)
                    || (item.getReponse() != null && item.getReponse().toLowerCase().contains(q))
                    || String.valueOf(item.getScore()).contains(q)
                    || String.valueOf(item.getIdQuestion()).contains(q);
            if (matches) {
                filtered.add(item);
            }
        }

        Comparator<Reponse> comparator = Comparator.comparingInt(Reponse::getIdReponse);
        String sort = sortChoice != null ? sortChoice.getValue() : "ID croissant";
        if ("ID décroissant".equals(sort)) {
            comparator = Comparator.comparingInt(Reponse::getIdReponse).reversed();
        } else if ("Réponse A-Z".equals(sort)) {
            comparator = Comparator.comparing(reponse -> safeValue(reponse.getReponse()), String.CASE_INSENSITIVE_ORDER);
        } else if ("Réponse Z-A".equals(sort)) {
            comparator = Comparator.comparing((Reponse reponse) -> safeValue(reponse.getReponse()), String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Score croissant".equals(sort)) {
            comparator = Comparator.comparingDouble(Reponse::getScore).thenComparingInt(Reponse::getIdReponse);
        } else if ("Score décroissant".equals(sort)) {
            comparator = Comparator.comparingDouble(Reponse::getScore).reversed().thenComparingInt(Reponse::getIdReponse);
        }
        FXCollections.sort(filtered, comparator);
        reponsesTable.setItems(filtered);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
