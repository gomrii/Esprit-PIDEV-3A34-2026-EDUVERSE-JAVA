package controller;

import Entities.Historique;
import Services.ServiceHistorique;
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
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreCours"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_consultation"));

        chargerHistorique();
    }

    private void chargerHistorique() {
        // On récupère la liste via le service (méthode de récupération avec JOIN)
        List<Historique> liste = service.getHistorique();
        tableHistorique.getItems().setAll(liste);
    }
}