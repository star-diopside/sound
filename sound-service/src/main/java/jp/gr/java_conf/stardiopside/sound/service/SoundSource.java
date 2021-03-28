package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.context.ApplicationEventPublisher;

public interface SoundSource {

    AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException;

    AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException;

    default void publishPlayBeginEvent(ApplicationEventPublisher publisher) {
    }

    default void publishPlayEndEvent(ApplicationEventPublisher publisher) {
    }

    default void publishSoundInformationEvent(ApplicationEventPublisher publisher) {
    }

    public static SoundSource of(Path path) {
        return new FileSoundSource(path);
    }

    public static SoundSource of(InputStream inputStream, String name) {
        return new InputStreamSoundSource(inputStream, name);
    }
}
