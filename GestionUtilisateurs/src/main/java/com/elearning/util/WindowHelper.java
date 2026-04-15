package com.elearning.util;

import javafx.stage.Stage;

public class WindowHelper {
    
    private static final double WIDTH = 900;
    private static final double HEIGHT = 600;
    
    public static void fixDimensions(Stage stage) {
        if (stage != null) {
            stage.setMinWidth(WIDTH);
            stage.setMinHeight(HEIGHT);
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
            stage.centerOnScreen();
        }
    }
    
    public static void fixDimensions(javafx.scene.Scene scene) {
        if (scene != null && scene.getWindow() != null) {
            fixDimensions((Stage) scene.getWindow());
        }
    }
}
