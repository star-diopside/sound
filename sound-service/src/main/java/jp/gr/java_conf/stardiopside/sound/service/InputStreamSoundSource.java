package jp.gr.java_conf.stardiopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import org.springframework.context.ApplicationEventPublisher;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;

class InputStreamSoundSource implements SoundSource {

    private static final Logger logger = Logger.getLogger(InputStreamSoundSource.class.getName());
    private final InputStream inputStream;
    private final String name;

    InputStreamSoundSource(InputStream inputStream, String name) {
        this.inputStream = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
        this.name = (name == null ? "unnamed" : name);
    }

    @Override
    public AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException {
        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioInputStream stream;
            try {
                logger.fine("Try AudioFileReader: " + reader.getClass());
                stream = reader.getAudioInputStream(inputStream);
            } catch (UnsupportedAudioFileException e) {
                logger.log(Level.FINEST, e.getMessage(), e);
                continue;
            }
            logger.fine("Using AudioFileReader: " + reader.getClass());
            return stream;
        }
        throw new UnsupportedAudioFileException("Stream of unsupported format");
    }

    @Override
    public AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException {
        for (var reader : ServiceLoader.load(AudioFileReader.class)) {
            AudioFileFormat format;
            try {
                logger.fine("Try AudioFileReader: " + reader.getClass());
                format = reader.getAudioFileFormat(inputStream);
            } catch (UnsupportedAudioFileException e) {
                logger.log(Level.FINEST, e.getMessage(), e);
                continue;
            }
            logger.fine("Using AudioFileReader: " + reader.getClass());
            return format;
        }
        throw new UnsupportedAudioFileException("Stream of unsupported format");
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
