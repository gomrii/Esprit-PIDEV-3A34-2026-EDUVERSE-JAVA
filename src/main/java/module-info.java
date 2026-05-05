module eduverse {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;
    requires java.net.http;
    requires com.google.gson;
    requires javafx.web;
    requires jdk.jsobject;
    requires java.desktop;
    requires javafx.swing;
    requires java.naming;
    requires jbcrypt;
    requires okhttp3;
    requires jakarta.mail;
    requires org.bytedeco.javacpp;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires org.bytedeco.openblas;

    opens Test to javafx.fxml;
    opens Controllers to javafx.fxml;
    opens Entities to javafx.base;
    opens com.elearning.controller to javafx.fxml;
    opens com.elearning.gui to javafx.fxml;
    opens com.elearning.entity to javafx.base;

    exports Test;
    exports Controllers;
    exports Entities;
    exports Services;
    exports Utils;
    exports com.elearning;
    exports com.elearning.controller;
    exports com.elearning.entity;
    exports com.elearning.service;
    exports com.elearning.util;
}
