module jp.gr.java_conf.stardiopside.sound.checker {
    opens jp.gr.java_conf.stardiopside.sound;
    opens jp.gr.java_conf.stardiopside.sound.controller;
    opens config;

    requires jp.gr.java_conf.stardiopside.sound.service;
    requires com.google.errorprone.annotations;
    requires jakarta.annotation;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires jul.to.slf4j;
    requires org.apache.commons.logging;
    requires org.controlsfx.controls;
    requires org.jspecify;
    requires org.slf4j;
    requires org.yaml.snakeyaml;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
}
