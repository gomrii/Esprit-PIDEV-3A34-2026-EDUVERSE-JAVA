package controller;

import Entities.Cours;
import Services.ServiceRecommandation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import java.util.Map;

public class RecommandationController {
    @FXML private VBox containerCards;
    @FXML private VBox listeHistorique;
    private ServiceRecommandation service = new ServiceRecommandation();

    public void initialize() {
        Map<Cours, String> data = service.genererSuggestions();

        containerCards.getChildren().clear();
        data.forEach((cours, raison) -> {
            containerCards.getChildren().add(creerCard(cours, raison));
        });
    }

    private HBox creerCard(Cours c, String raison) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        VBox v = new VBox(8);
        Label t = new Label(c.getTitle());
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 17px;");

        Label lvl = new Label(c.getLevel());
        lvl.setStyle("-fx-background-color: #F3E5F5; -fx-text-fill: #7B1FA2; -fx-padding: 2 10; -fx-background-radius: 10;");

        Label r = new Label(raison);
        r.setWrapText(true);
        r.setStyle("-fx-text-fill: #5D4E9C; -fx-font-style: italic; -fx-background-color: #F8F7FF; -fx-padding: 5;");

        v.getChildren().addAll(t, lvl, r);

        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        Button b = new Button("Ouvrir");
        b.setStyle("-fx-background-color: #3F3D56; -fx-text-fill: white; -fx-background-radius: 15;");

        card.getChildren().addAll(v, s, b);
        return card;
    }
}
