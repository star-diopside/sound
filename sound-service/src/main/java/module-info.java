module jp.gr.java_conf.stardiopside.sound.service {
    exports jp.gr.java_conf.stardiopside.sound.service;
    exports jp.gr.java_conf.stardiopside.sound.event;

    requires static lombok;
    requires jp.gr.java_conf.stardiopside.sound.util;
    requires jp.gr.java_conf.stardiopside.sound.compatibility;
    requires transitive java.desktop;
    requires java.annotation;
    requires org.slf4j;
    requires spring.core;
    requires spring.context;

    uses javax.sound.sampled.spi.AudioFileReader;
}
