module com.oud.oudshopify {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires mapdb;
    requires htmlunit;
    requires com.google.gson;
    requires MaterialFX;
    requires static lombok;
    requires atlantafx.base;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;


    opens com.oud.oudshopify to javafx.fxml;
    opens com.oud.oudshopify.controllers.filterable;
    opens com.oud.oudshopify.data;
    exports com.oud.oudshopify;
    exports com.oud.oudshopify.controllers;
    opens com.oud.oudshopify.controllers to javafx.fxml;
}