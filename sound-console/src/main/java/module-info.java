module jp.gr.java_conf.stardiopside.sound.console {
    opens jp.gr.java_conf.stardiopside.sound;
    opens config;

    requires jp.gr.java_conf.stardiopside.sound.service;
    requires jp.gr.java_conf.stardiopside.sound.util;
    requires java.annotation;
    requires org.slf4j;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
}
