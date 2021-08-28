package jp.gr.java_conf.stardiopside.sound.service;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.stream.Stream;

class InputStreamSoundSource implements SoundSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamSoundSource.class);
    private final InputStream inputStream;
    private final String name;

    InputStreamSoundSource(InputStream inputStream, String name) {
        this.inputStream = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
        this.name = (name == null ? "unnamed" : name);
    }

    @Override
    public AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException {
        Stream.Builder<Exception> exceptions = Stream.builder();

        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioInputStream stream;
            try {
                LOGGER.debug("Try AudioFileReader: " + reader.getClass());
                stream = reader.getAudioInputStream(inputStream);
            } catch (UnsupportedAudioFileException | IOException e) {
                LOGGER.trace(e.getMessage(), e);
                exceptions.add(e);
                continue;
            }
            LOGGER.debug("Using AudioFileReader: " + reader.getClass());
            return stream;
        }

        var exc = new UnsupportedAudioFileException("Stream of unsupported format");
        exceptions.build().forEach(exc::addSuppressed);
        throw exc;
    }

    @Override
    public AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException {
        Stream.Builder<Exception> exceptions = Stream.builder();

        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioFileFormat format;
            try {
                LOGGER.debug("Try AudioFileReader: " + reader.getClass());
                format = reader.getAudioFileFormat(inputStream);
            } catch (UnsupportedAudioFileException | IOException e) {
                LOGGER.trace(e.getMessage(), e);
                exceptions.add(e);
                continue;
            }
            LOGGER.debug("Using AudioFileReader: " + reader.getClass());
            return format;
        }

        var exc = new UnsupportedAudioFileException("Stream of unsupported format");
        exceptions.build().forEach(exc::addSuppressed);
        throw exc;
    }

    @Override
    public void publishPlayBeginEvent(ApplicationEventPublisher publisher) {
        publisher.publishEvent(new SoundActionEvent("BEGIN", name));
    }

    @Override
    public void publishPlayEndEvent(ApplicationEventPublisher publisher) {
        publisher.publishEvent(new SoundActionEvent("END", name));
    }

    @Override
    public String toString() {
        return "InputStreamSoundSource[inputStream=" + inputStream + ", name=" + name + "]";
    }
}
