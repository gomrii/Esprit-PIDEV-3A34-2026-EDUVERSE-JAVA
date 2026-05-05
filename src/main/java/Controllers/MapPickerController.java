package Controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class MapPickerController {

    @FXML private WebView webView;
    
    private String selectedAddress = "";
    private AjouterEventController parentController;

    @FXML
    public void initialize() {
        WebEngine engine = webView.getEngine();
        
        // Permettre à WebEngine d'afficher les alertes JavaScript
        engine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));
        
        // Charger le fichier HTML de la carte
        String url = getClass().getResource("/html/map.html").toExternalForm();
        engine.load(url);

        // Pont entre JavaScript et Java
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("app", new Bridge());
                
                // Rediriger les erreurs JS vers notre Bridge Java
                engine.executeScript("window.onerror = function(message, source, lineno, colno, error) { app.logError(message + ' at ' + source + ':' + lineno); return true; };");
                
                // Rediriger console.log vers Java pour faciliter le debug
                engine.executeScript("console.log = function(message) { app.logInfo(message); };");
            } else if (newState == Worker.State.FAILED) {
                System.err.println("Erreur de chargement de la page : " + engine.getLoadWorker().getException());
            }
        });
    }

    public void setParentController(AjouterEventController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    @FXML
    private void handleConfirm() {
        if (parentController != null && !selectedAddress.isEmpty()) {
            parentController.setLocationFromMap(selectedAddress);
        }
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close();
    }

    /**
     * Classe interne servant de pont pour recevoir les données du JavaScript
     */
    public class Bridge {
        public void onLocationSelected(String address) {
            selectedAddress = address;
            System.out.println("Adresse sélectionnée sur la carte : " + address);
            
            // Fermer la fenêtre et mettre à jour le parent
            javafx.application.Platform.runLater(() -> {
                if (parentController != null) {
                    parentController.setLocationFromMap(address);
                }
                closeStage();
            });
        }
        
        public void onCancel() {
            System.out.println("Sélection annulée par l'utilisateur.");
            javafx.application.Platform.runLater(() -> closeStage());
        }
        
        // Méthode pour capturer les console.log de JS
        public void logInfo(String message) {
            System.out.println("[JS INFO] " + message);
        }
        
        // Méthode pour capturer les erreurs JS
        public void logError(String message) {
            System.err.println("[JS ERROR] " + message);
        }
    }
}
