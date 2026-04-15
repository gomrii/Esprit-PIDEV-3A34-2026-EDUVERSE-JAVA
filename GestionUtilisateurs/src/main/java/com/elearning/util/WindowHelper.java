package com.elearning.util;

import javafx.stage.Stage;

public class WindowHelper {
    
    private static final double WIDTH = 900;
    private static final double HEIGHT = 600;
    
    public static void fixDimensions(Stage stage) {
        if (stage != null) {
            // Forcer les dimensions de manière agressive
            stage.setMinWidth(WIDTH);
            stage.setMinHeight(HEIGHT);
            stage.setMaxWidth(WIDTH);
            stage.setMaxHeight(HEIGHT);
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
            stage.centerOnScreen();
        }
    }
}
