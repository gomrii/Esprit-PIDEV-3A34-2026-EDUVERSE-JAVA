package com.elearning.controller;

import com.elearning.dao.UserDAO;
import com.elearning.entity.User;
import com.elearning.service.FaceRecognitionService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class FaceIdSimulatorController implements Initializable {

    @FXML private ImageView   webcamView;
    @FXML private ProgressBar scanProgressBar;
    @FXML private Label       statusLabel;
    @FXML private Label       countLabel;
    @FXML private Button      btnAnnuler;

    private String          targetEmail;
    private int             targetUserId = -1;
    private LoginController parentController;
    private final UserDAO              userDAO     = new UserDAO();
    private final FaceRecognitionService faceService = new FaceRecognitionService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (scanProgressBar != null) scanProgressBar.setProgress(0);
        if (statusLabel != null) statusLabel.setText("🎥 Initialisation...");
        if (countLabel != null) countLabel.setText("");
    }

    public void demarrerScan(String email, LoginController parent) {
        this.targetEmail      = email;
        this.parentController = parent;

        User user = userDAO.trouverParEmail(email.trim().toLowerCase());
        if (user != null) this.targetUserId = user.getId();

        boolean modeEnregistrement = targetUserId <= 0
            || !faceService.visageEnregistre(targetUserId);

        if (modeEnregistrement) {
            statusLabel.setText("📸 Mode enregistrement — regardez la caméra");
            statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            lancerEnregistrement();
        } else {
            statusLabel.setText("🔍 Mode vérification — regardez la caméra");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            lancerVerification();
        }
    }

    private void lancerEnregistrement() {
        FaceRecognitionService.FrameCallback frameCallback = image ->
            Platform.runLater(() -> {
                if (webcamView != null) webcamView.setImage(image);
            });

        FaceRecognitionService.ProgressCallback progressCb =
            new FaceRecognitionService.ProgressCallback() {
                @Override
                public void onProgress(String message, int done, int total) {
                    Platform.runLater(() -> {
                        if (statusLabel != null) statusLabel.setText(message);
                        if (total > 0 && scanProgressBar != null) {
                            scanProgressBar.setProgress((double) done / total);
                            if (countLabel != null)
                                countLabel.setText(done + "/" + total);
                        }
                    });
                }
                @Override
                public void onSuccess(String message) {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText(message);
                            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                        if (scanProgressBar != null) {
                            scanProgressBar.setProgress(1.0);
                            scanProgressBar.setStyle("-fx-accent: #27ae60;");
                        }
                        new Timeline(new KeyFrame(Duration.seconds(2),
                            e -> fermerFenetre())).play();
                    });
                }
                @Override
                public void onError(String message) {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText(message);
                            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                        if (scanProgressBar != null) {
                            scanProgressBar.setProgress(0);
                            scanProgressBar.setStyle("-fx-accent: #e74c3c;");
                        }
                    });
                }
            };

        faceService.enregistrerVisage(targetUserId, frameCallback, progressCb);
    }

    private void lancerVerification() {
        FaceRecognitionService.FrameCallback frameCallback = image ->
            Platform.runLater(() -> {
                if (webcamView != null) webcamView.setImage(image);
            });

        FaceRecognitionService.ProgressCallback progressCb =
            new FaceRecognitionService.ProgressCallback() {
                @Override
                public void onProgress(String message, int done, int total) {
                    Platform.runLater(() -> {
                        if (statusLabel != null) statusLabel.setText(message);
                    });
                }
                @Override
                public void onSuccess(String message) {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText("✅ Visage reconnu !");
                            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                        if (scanProgressBar != null) {
                            scanProgressBar.setProgress(1.0);
                            scanProgressBar.setStyle("-fx-accent: #27ae60;");
                        }

                        User user = userDAO.trouverParEmail(targetEmail.trim().toLowerCase());
                        new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
                            fermerFenetre();
                            if (user != null && parentController != null)
                                parentController.validerFaceId(user);
                        })).play();
                    });
                }
                @Override
                public void onError(String message) {
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setText(message);
                            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                        if (scanProgressBar != null) {
                            scanProgressBar.setProgress(1.0);
                            scanProgressBar.setStyle("-fx-accent: #e74c3c;");
                        }
                        new Timeline(new KeyFrame(Duration.seconds(2.5),
                            e -> fermerFenetre())).play();
                    });
                }
            };

        faceService.verifierVisage(targetUserId, frameCallback, progressCb);
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        fermerFenetre();
    }

    private void fermerFenetre() {
        try {
            ((Stage) btnAnnuler.getScene().getWindow()).close();
        } catch (Exception ignored) {}
    }
}
