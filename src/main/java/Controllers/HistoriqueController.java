package Controllers;

import Entities.Historique;
import Services.ServiceHistorique;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class HistoriqueController {

    @FXML private TableView<Historique> tableHistorique;
    @FXML private TableColumn<Historique, String> colTitre;
    @FXML private TableColumn<Historique, String> colDate;

    private final ServiceHistorique service = new ServiceHistorique();

    @FXML
    public void initialize() {
        // Liaison des colonnes avec les attributs de l'entité Historique
        // Note : Assurez-vous que l'entité Historique possède les getters getTitreCours() et getDate_consultation()
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreCours"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_consultation"));

        chargerHistorique();
    }

    private void chargerHistorique() {
        // Récupération de la liste via le service
        List<Historique> liste = service.getHistorique();
        tableHistorique.getItems().setAll(liste);
    }

    @FXML
    void retourListe(ActionEvent event) {
        // ✅ NAVIGATION HARMONISÉE : Retourne à la liste des cours sans casser le Dashboard
        MainDashboardController.getInstance().loadView("AffichierCoursStudent.fxml", "Mes Cours");
    }

    @FXML
    void goToDash(ActionEvent event) {
        MainDashboardController.getInstance().loadView(ControllerUtils.getRoleBasedDashboardFxml(), "Tableau de Bord");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        MainDashboardController.getInstance().handleLogout();
    }
}