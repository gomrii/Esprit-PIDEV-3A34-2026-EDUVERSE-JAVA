module eduverse {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;

    opens Test to javafx.fxml;
    opens Controllers to javafx.fxml;
    opens Entities to javafx.base;

    exports Test;
    exports Controllers;
    exports Entities;
    exports Services;
    exports Utils;
}
