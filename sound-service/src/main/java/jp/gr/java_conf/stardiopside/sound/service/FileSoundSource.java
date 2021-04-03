package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import org.springframework.context.ApplicationEventPublisher;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;

class FileSoundSource implements SoundSource {

    private static final Logger logger = Logger.getLogger(FileSoundSource.class.getName());
    private final Path path;

    FileSoundSource(Path path) {
        this.path = path;
    }

    @Override
    public AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException {
        var file = path.toFile();
        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioInputStream stream;
            try {
                logger.fine("Try AudioFileReader: " + reader.getClass());
                stream = reader.getAudioInputStream(file);
            } catch (UnsupportedAudioFileException e) {
                logger.log(Level.FINEST, e.getMessage(), e);
                continue;
            }
            logger.fine("Using AudioFileReader: " + reader.getClass());
            return stream;
        }
        throw new UnsupportedAudioFileException("File of unsupported format");
    }

    @Override
    public AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException {
        var file = path.toFile();
        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioFileFormat format;
            try {
                logger.fine("Try AudioFileReader: " + reader.getClass());
                format = reader.getAudioFileFormat(file);
            } catch (UnsupportedAudioFileException e) {
                logger.log(Level.FINEST, e.getMessage(), e);
                continue;
            }
            logger.fine("Using AudioFileReader: " + reader.getClass());
            return format;
        }
        throw new UnsupportedAudioFileException("File of unsupported format");
    }

    @Override
    public Optional<SoundInformation> getSoundInformation() throws Exception {
        return Optional.of(new SoundInformation(path));
    }

    @Override
    public void publishPlayBeginEvent(ApplicationEventPublisher publisher) {
        publisher.publishEvent(new SoundActionEvent("BEGIN", path));
    }

    @Override
    public void publishPlayEndEvent(ApplicationEventPublisher publisher) {
        publisher.publishEvent(new SoundActionEvent("END", path));
    }

    @Override
    public String toString() {
        return "FileSoundSource[path=" + path + "]";
    }
}
