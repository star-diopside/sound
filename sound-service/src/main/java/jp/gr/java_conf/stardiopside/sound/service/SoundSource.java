package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.context.ApplicationEventPublisher;

import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformationEvent;

public interface SoundSource {

    AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException;

    AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException;

    default Optional<SoundInformation> getSoundInformation() throws Exception {
        return Optional.empty();
    }

    default void publishPlayBeginEvent(ApplicationEventPublisher publisher) {
    }

    default void publishPlayEndEvent(ApplicationEventPublisher publisher) {
    }

    default void publishSoundInformationEvent(ApplicationEventPublisher publisher) {
        try {
            getSoundInformation().map(SoundInformationEvent::new).ifPresent(publisher::publishEvent);
        } catch (Exception e) {
            SoundSourceLogger.logger.log(Level.WARNING, e.getMessage(), e);
            publisher.publishEvent(new SoundExceptionEvent(e, this));
        }
    }

    public static SoundSource of(Path path) {
        return new FileSoundSource(path);
    }

    public static SoundSource of(InputStream inputStream, String name) {
        return new InputStreamSoundSource(inputStream, name);
    }
}

final class SoundSourceLogger {
    static final Logger logger = Logger.getLogger(SoundSource.class.getName());
}
