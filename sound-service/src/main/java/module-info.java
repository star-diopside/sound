module jp.gr.java_conf.stardiopside.sound.service {
    exports jp.gr.java_conf.stardiopside.sound.service;
    exports jp.gr.java_conf.stardiopside.sound.event;

    requires static lombok;
    requires transitive java.desktop;
    requires java.logging;
    requires java.annotation;
    requires spring.core;
    requires spring.context;
    requires jaudiotagger;
}
