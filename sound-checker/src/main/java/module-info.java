module jp.gr.java_conf.stardiopside.sound.checker {
    opens jp.gr.java_conf.stardiopside.sound;
    opens jp.gr.java_conf.stardiopside.sound.controller;
    opens config;

    requires jp.gr.java_conf.stardiopside.sound.service;
    requires jp.gr.java_conf.stardiopside.sound.util;
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
