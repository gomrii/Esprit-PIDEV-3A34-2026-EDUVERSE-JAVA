module eduverse {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;
    requires java.net.http;
    requires com.google.gson;
    requires javafx.web;
    requires jdk.jsobject;

    opens Test to javafx.fxml;
    opens Controllers to javafx.fxml;
    opens Entities to javafx.base;

    exports Test;
    exports Controllers;
    exports Entities;
    exports Services;
    exports Utils;
}
