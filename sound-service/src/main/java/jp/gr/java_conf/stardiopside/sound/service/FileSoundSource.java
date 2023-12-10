package jp.gr.java_conf.stardiopside.sound.service;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;
import jp.gr.java_conf.stardiopside.sound.internal.SoundInformations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

class FileSoundSource implements SoundSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSoundSource.class);
    private final Path path;

    FileSoundSource(Path path) {
        this.path = path;
    }

    @Override
    public AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException {
        var file = path.toFile();
        Stream.Builder<Exception> exceptions = Stream.builder();

        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioInputStream stream;
            try {
                LOGGER.debug("Try AudioFileReader: " + reader.getClass());
                stream = reader.getAudioInputStream(file);
            } catch (UnsupportedAudioFileException | IOException e) {
                LOGGER.trace(e.getMessage(), e);
                exceptions.add(e);
                continue;
            }
            LOGGER.debug("Using AudioFileReader: " + reader.getClass());
            return stream;
        }

        var exc = new UnsupportedAudioFileException("File of unsupported format");
        exceptions.build().forEach(exc::addSuppressed);
        throw exc;
    }

    @Override
    public AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException {
        var file = path.toFile();
        Stream.Builder<Exception> exceptions = Stream.builder();

        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioFileFormat format;
            try {
                LOGGER.debug("Try AudioFileReader: " + reader.getClass());
                format = reader.getAudioFileFormat(file);
            } catch (UnsupportedAudioFileException | IOException e) {
                LOGGER.trace(e.getMessage(), e);
                exceptions.add(e);
                continue;
            }
            LOGGER.debug("Using AudioFileReader: " + reader.getClass());
            return format;
        }

        var exc = new UnsupportedAudioFileException("File of unsupported format");
        exceptions.build().forEach(exc::addSuppressed);
        throw exc;
    }

    @Override
    public Optional<SoundInformation> getSoundInformation() throws Exception {
        return SoundInformations.getFromCache(path);
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
