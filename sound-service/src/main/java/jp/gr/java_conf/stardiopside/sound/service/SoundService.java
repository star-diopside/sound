package jp.gr.java_conf.stardiopside.sound.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;

public interface SoundService {

    Duration getPosition();

    void play(Path path);

    void play(InputStream inputStream, String name);

    void skip();

}
