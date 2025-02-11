package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;

public interface SoundService {

    Duration getPosition();

    boolean play(SoundSource soundSource);

    default boolean play(Path path) throws IOException {
        try (var soundSource = SoundSource.of(path)) {
            return play(soundSource);
        }
    }

    default boolean play(InputStream inputStream, String name) throws IOException {
        try (var soundSource = SoundSource.of(inputStream, name)) {
            return play(soundSource);
        }
    }

    void skip();

}
