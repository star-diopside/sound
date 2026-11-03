module jp.gr.java_conf.stardiopside.sound.service {
    exports jp.gr.java_conf.stardiopside.sound.service;
    exports jp.gr.java_conf.stardiopside.sound.event;
    exports jp.gr.java_conf.stardiopside.sound.util;

    requires jp.gr.java_conf.stardiopside.sound.compatibility;
    requires com.google.common;
    requires jakarta.annotation;
    requires org.apache.commons.lang3;
    requires org.jspecify;
    requires org.slf4j;
    requires spring.context;
    requires spring.core;
    requires transitive java.desktop;
    requires static lombok;

    uses javax.sound.sampled.spi.AudioFileReader;
}
