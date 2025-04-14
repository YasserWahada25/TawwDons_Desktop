module org.example.event {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;

    opens org.example.event to javafx.fxml;
    exports org.example.event;
    exports org.example.event.test;
    exports org.example.event.controller;
    exports org.example.event.model;

    opens org.example.event.model to javafx.base;
    opens org.example.event.controller to javafx.fxml;



}
