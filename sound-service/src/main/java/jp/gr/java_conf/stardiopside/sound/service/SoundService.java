package jp.gr.java_conf.stardiopside.sound.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;

public interface SoundService {

    Duration getPosition();

    boolean play(Path path);

    boolean play(InputStream inputStream, String name);

    void skip();

}
