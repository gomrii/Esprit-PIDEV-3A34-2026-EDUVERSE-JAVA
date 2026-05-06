package com.elearning.util;

import javafx.stage.Stage;
import javafx.scene.Node;

public class WindowHelper {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 600;
    private static double dragOffsetX;
    private static double dragOffsetY;

    public static void fixDimensions(Stage stage) {
        if (stage != null) {
            // Ne pas bloquer le redimensionnement manuel
            stage.setMinWidth(WIDTH);
            stage.setMinHeight(HEIGHT);
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
            stage.centerOnScreen();
        }
    }

    public static void enableWindowDrag(Node dragHandle, Stage stage) {
        if (dragHandle == null || stage == null) {
            return;
        }

        dragHandle.setOnMousePressed(event -> {
            dragOffsetX = event.getSceneX();
            dragOffsetY = event.getSceneY();
        });

        dragHandle.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        });
    }
}
