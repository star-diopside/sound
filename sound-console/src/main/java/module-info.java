module jp.gr.java_conf.stardiopside.sound.console {
    opens jp.gr.java_conf.stardiopside.sound;
    opens config;

    requires jp.gr.java_conf.stardiopside.sound.service;
    requires jakarta.annotation;
    requires org.slf4j;
    requires org.yaml.snakeyaml;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
}
