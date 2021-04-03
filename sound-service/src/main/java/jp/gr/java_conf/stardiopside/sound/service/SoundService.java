package jp.gr.java_conf.stardiopside.sound.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;

public interface SoundService {

    Duration getPosition();

    boolean play(SoundSource soundSource);

    default boolean play(Path path) {
        return play(SoundSource.of(path));
    }

    default boolean play(InputStream inputStream, String name) {
        return play(SoundSource.of(inputStream, name));
    }

    void skip();

}
