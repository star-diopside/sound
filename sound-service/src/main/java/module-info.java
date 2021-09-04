module jp.gr.java_conf.stardiopside.sound.service {
    exports jp.gr.java_conf.stardiopside.sound.service;
    exports jp.gr.java_conf.stardiopside.sound.event;
    exports jp.gr.java_conf.stardiopside.sound.util;

    requires static lombok;
    requires jp.gr.java_conf.stardiopside.sound.compatibility;
    requires transitive java.desktop;
    requires java.annotation;
    requires com.google.common;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires spring.core;
    requires spring.context;

    uses javax.sound.sampled.spi.AudioFileReader;
}
