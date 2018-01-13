package jp.gr.java_conf.stardiopside.sound.service;

import java.nio.file.Path;

public interface SoundPlayer {

    void terminate();

    void play();

    void skip();

    void stop();

    void back();

    void clear();

    void add(Path path);

}
