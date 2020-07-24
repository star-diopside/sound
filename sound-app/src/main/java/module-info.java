module jp.gr.java_conf.stardiopside.sound.app {
    opens jp.gr.java_conf.stardiopside.sound;
    opens jp.gr.java_conf.stardiopside.sound.controller;
    opens jp.gr.java_conf.stardiopside.sound.model to javafx.base;
    opens config;

    requires jp.gr.java_conf.stardiopside.sound.service;
    requires java.logging;
    requires java.annotation;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
}
