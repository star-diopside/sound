package jp.gr.java_conf.stardiopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundFinishEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import lombok.Getter;

@Service
public class SoundServiceImpl implements SoundService {

    private static final Logger logger = Logger.getLogger(SoundServiceImpl.class.getName());
    private volatile boolean skipping;

    @Getter
    private volatile Duration position;

    private final ApplicationEventPublisher publisher;

    public SoundServiceImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public boolean play(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return play(is, path.getFileName().toString());
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            publisher.publishEvent(new SoundExceptionEvent(e));
            return false;
        }
    }

    @Override
    public boolean play(InputStream inputStream, String name) {
        String title = (name == null ? "unnamed" : name);
        publisher.publishEvent(new SoundActionEvent("Begin " + title));
        try {
            InputStream is = markSupportedInputStream(inputStream);
            getTitle(is).ifPresent(s -> publisher.publishEvent(new SoundActionEvent("Title: " + s)));
            play(is);
            return true;
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            publisher.publishEvent(new SoundExceptionEvent(e));
            return false;
        } finally {
            publisher.publishEvent(new SoundActionEvent("End " + title));
        }
    }

    private void play(InputStream inputStream)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        try (AudioInputStream baseInputStream = AudioSystem
                .getAudioInputStream(markSupportedInputStream(inputStream))) {
            AudioFormat baseFormat = baseInputStream.getFormat();
            publisher.publishEvent(new SoundActionEvent("INPUT: " + baseFormat.getClass() + " - " + baseFormat));
            if (baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                    || baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
                play(baseInputStream, baseFormat);
            } else {
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                        16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                publisher.publishEvent(
                        new SoundActionEvent("DECODED: " + decodedFormat.getClass() + " - " + decodedFormat));
                try (AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat,
                        baseInputStream)) {
                    play(decodedInputStream, decodedFormat);
                }
            }
        }
    }

    private void play(AudioInputStream inputStream, AudioFormat format) throws IOException, LineUnavailableException {
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            skipping = false;
            line.addLineListener(event -> publisher.publishEvent(new SoundLineEvent(event)));
            line.open(format);
            line.start();
            byte[] data = new byte[line.getBufferSize()];
            int size;
            while (!skipping && (size = inputStream.read(data, 0, data.length)) != -1) {
                line.write(data, 0, size);
                Duration pos = Duration.of(line.getMicrosecondPosition(), ChronoUnit.MICROS);
                position = pos;
                publisher.publishEvent(new SoundPositionEvent(pos));
            }
            line.drain();
            line.stop();
            position = null;
            publisher.publishEvent(SoundFinishEvent.INSTANCE);
        } finally {
            skipping = false;
        }
    }

    private Optional<String> getTitle(InputStream inputStream) throws IOException, UnsupportedAudioFileException {
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(markSupportedInputStream(inputStream));
        Object title = audioFileFormat.properties().get("title");
        return title == null ? Optional.empty() : Optional.of(title.toString());
    }

    private static InputStream markSupportedInputStream(InputStream inputStream) {
        return inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
    }

    @Override
    public void skip() {
        skipping = true;
    }
}
