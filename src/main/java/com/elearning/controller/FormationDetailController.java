package com.elearning.controller;

import com.elearning.entity.Formation;
import com.elearning.entity.Ressource;
import com.elearning.entity.User;
import com.elearning.service.FormationService;
import com.elearning.service.GroqAIService;
import com.elearning.service.RessourceService;
import com.elearning.service.StripePaymentService;
import com.elearning.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class FormationDetailController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label levelLabel;
    @FXML private Label priceLabel;
    @FXML private Label durationLabel;
    @FXML private Label creatorLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea contentArea;
    @FXML private TableView<Ressource> ressourcesTable;
    @FXML private TableColumn<Ressource, String> colRessTitle;
    @FXML private TableColumn<Ressource, String> colRessType;
    @FXML private TableColumn<Ressource, String> colRessUrl;
    @FXML private VBox ressourcesSection;
    @FXML private TableView<Map<String, String>> enrolledStudentsTable;
    @FXML private TableColumn<Map<String, String>, String> colStudentName;
    @FXML private TableColumn<Map<String, String>, String> colStudentEmail;
    @FXML private VBox enrolledStudentsSection;
    @FXML private Button btnEnroll;
    @FXML private Button btnFreeEnroll;
    @FXML private Button btnBack;
    @FXML private Label enrollStatusLabel;

    private final FormationService formationService = new FormationService();
    private final RessourceService ressourceService = new RessourceService();
    private final StripePaymentService stripePaymentService = new StripePaymentService();
    private Formation currentFormation;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.currentUser = SessionManager.getInstance().getUtilisateurConnecte();
        
        // Setup table columns
        colRessTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colRessType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRessUrl.setCellValueFactory(new PropertyValueFactory<>("url"));

        // Setup enrolled students table columns
        colStudentName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("studentName")));
        colStudentEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("studentEmail")));

        btnEnroll.setOnAction(event -> handleBuyFormation());
        btnFreeEnroll.setOnAction(event -> handleFreeEnroll());
        btnBack.setOnAction(event -> goBack());
    }

    public void setFormation(Formation formation) {
        this.currentFormation = formation;
        displayFormationDetails();
    }

    private void displayFormationDetails() {
        titleLabel.setText(currentFormation.getTitle());
        levelLabel.setText("Niveau: " + currentFormation.getLevel());
        priceLabel.setText("Prix: " + currentFormation.getPrice() + " TND");
        durationLabel.setText("Durée: " + currentFormation.getDuration() + " heures");
        creatorLabel.setText("Créé par: " + currentFormation.getCreatorName());
        descriptionArea.setText(currentFormation.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        contentArea.setText(currentFormation.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);

        // Check if user is enrolled or is the creator/admin
        boolean isEnrolled = false;
        boolean isCreator = currentUser.getId() == currentFormation.getCreatorId();
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean isPaidFormation = currentFormation.getPrice() > 0;

        if ("ETUDIANT".equals(currentUser.getRole())) {
            isEnrolled = formationService.isStudentEnrolled(currentFormation.getId(), currentUser.getId());
        }

        // Show resources only if: creator/admin, or enrolled student
        if (isCreator || isAdmin || isEnrolled) {
            ressourcesSection.setVisible(true);
            loadRessources();
            btnEnroll.setVisible(false);
            btnFreeEnroll.setVisible(false);
            enrollStatusLabel.setText("Vous avez accès à cette formation");
        } else {
            ressourcesSection.setVisible(false);
            btnEnroll.setVisible(isPaidFormation && "ETUDIANT".equals(currentUser.getRole()));
            btnFreeEnroll.setVisible(!isPaidFormation && "ETUDIANT".equals(currentUser.getRole()));
            if (isPaidFormation) {
                enrollStatusLabel.setText("Formation payante - Les ressources seront visibles après l'achat");
            } else {
                enrollStatusLabel.setText("Les ressources seront visibles après inscription");
            }
        }

        // Show enrolled students section only to creator/admin
        if (isCreator || isAdmin) {
            enrolledStudentsSection.setVisible(true);
            loadEnrolledStudents();
        } else {
            enrolledStudentsSection.setVisible(false);
        }
    }

    private void loadRessources() {
        ObservableList<Ressource> ressources = FXCollections.observableArrayList(
                formationService.getRessourcesForFormation(currentFormation.getId())
        );
        ressourcesTable.setItems(ressources);
    }

    private void loadEnrolledStudents() {
        ObservableList<Map<String, String>> students = FXCollections.observableArrayList(
                formationService.getEnrolledStudentsForFormation(currentFormation.getId())
        );
        enrolledStudentsTable.setItems(students);
    }

    @FXML
    private void handleBuyFormation() {
        try {
            // Find the minimum credit package that covers the formation price
            int creditsNeeded = stripePaymentService.findMinimumCreditPackageForPrice(currentFormation.getPrice());
            
            // Get the actual price of this credit package
            Map<Integer, Integer> packages = stripePaymentService.getAvailableCreditPackages();
            int priceInCents = packages.get(creditsNeeded);
            double actualPrice = priceInCents / 100.0;
            
            String priceInfo = String.format("%.2f", currentFormation.getPrice());
            String packagePriceInfo = String.format("%.2f", actualPrice);
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Purchase");
            confirmAlert.setHeaderText("Buy Formation");
            String message = "Formation: " + currentFormation.getTitle() + 
                    "\n\nFormation Price: " + priceInfo + " TND" +
                    "\nCredit Package: " + creditsNeeded + " credits" +
                    "\nYou will pay: $" + packagePriceInfo;
            confirmAlert.setContentText(message);
            
            if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                return;
            }
            
            // Stripe requires success and cancel URLs, even for desktop apps
            // These won't be used for redirects in a desktop environment, but Stripe needs them
            String successUrl = "https://eduverse.local/payment/success";
            String cancelUrl = "https://eduverse.local/payment/cancel";
            
            StripePaymentService.StripeCheckoutResponse response = stripePaymentService.createCheckoutSession(
                    currentUser.getId(), 
                    creditsNeeded,
                    successUrl,
                    cancelUrl
            );
            
            if (response != null && response.checkoutUrl != null && !response.checkoutUrl.isEmpty()) {
                // Open Stripe checkout in default browser
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    Runtime.getRuntime().exec("cmd /c start " + response.checkoutUrl);
                } else if (osName.contains("mac")) {
                    Runtime.getRuntime().exec("open " + response.checkoutUrl);
                } else if (osName.contains("nix") || osName.contains("nux")) {
                    Runtime.getRuntime().exec("xdg-open " + response.checkoutUrl);
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Payment in Progress");
                alert.setHeaderText("Complete Payment on Stripe");
                alert.setContentText("A Stripe checkout page has opened in your browser.\n\n" +
                        "1. Complete the payment on the Stripe page\n" +
                        "2. Your payment will be processed\n" +
                        "3. You will have access to the formation once payment is confirmed\n\n" +
                        "Close this dialog to return to the app.");
                alert.showAndWait();
                
                // Refresh the formation view to check if user now has access
                displayFormationDetails();
                
            } else {
                throw new Exception("Failed to create checkout session");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Payment Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleFreeEnroll() {
        try {
            boolean success = formationService.enrollStudent(currentFormation.getId(), currentUser.getId());
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Enrollment Successful");
                alert.setContentText("You are now enrolled in this formation!");
                alert.showAndWait();
                displayFormationDetails(); // Refresh to show resources
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Enrollment Failed");
                alert.setContentText("You are already enrolled in this formation or an error occurred.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error During Enrollment");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleEnroll() {
        // This method is kept for backward compatibility
        // It will call handleFreeEnroll if the formation is free, or handleBuyFormation if it's paid
        if (currentFormation.getPrice() > 0) {
            handleBuyFormation();
        } else {
            handleFreeEnroll();
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}
