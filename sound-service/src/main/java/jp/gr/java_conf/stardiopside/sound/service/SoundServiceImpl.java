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

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformationEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionFinishEvent;
import lombok.Getter;

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
        String title = path.getFileName().toString();
        publisher.publishEvent(new SoundActionEvent("Begin " + title));

        try {
            try {
                AudioFile audioFile = AudioFileIO.read(path.toFile());
                publisher.publishEvent(new SoundInformationEvent(audioFile));
            } catch (CannotReadException | IOException | TagException | ReadOnlyFileException
                    | InvalidAudioFrameException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                publisher.publishEvent(new SoundExceptionEvent(e));
                return false;
            }

            try (InputStream is = Files.newInputStream(path)) {
                return playInternal(is);
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                publisher.publishEvent(new SoundExceptionEvent(e));
                return false;
            }

        } finally {
            publisher.publishEvent(new SoundActionEvent("End " + title));
        }
    }

    @Override
    public boolean play(InputStream inputStream, String name) {
        String title = (name == null ? "unnamed" : name);
        publisher.publishEvent(new SoundActionEvent("Begin " + title));

        try {
            return playInternal(inputStream);
        } finally {
            publisher.publishEvent(new SoundActionEvent("End " + title));
        }
    }

    private boolean playInternal(InputStream inputStream) {
        try {
            InputStream is = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
            getTitle(is).ifPresent(s -> publisher.publishEvent(new SoundActionEvent("Title: " + s)));
            playBufferedInputStream(is);
            return true;
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            publisher.publishEvent(new SoundExceptionEvent(e));
            return false;
        }
    }

    private void playBufferedInputStream(InputStream inputStream)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        Assert.isTrue(inputStream.markSupported(), "inputStream does not support mark.");
        try (AudioInputStream baseInputStream = AudioSystem.getAudioInputStream(inputStream)) {
            AudioFormat baseFormat = baseInputStream.getFormat();
            publisher.publishEvent(new SoundActionEvent("INPUT: " + baseFormat.getClass() + " - " + baseFormat));
            if (baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                    || baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
                playAudioInputStream(baseInputStream, baseFormat);
            } else {
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                        16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                publisher.publishEvent(
                        new SoundActionEvent("DECODED: " + decodedFormat.getClass() + " - " + decodedFormat));
                try (AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat,
                        baseInputStream)) {
                    playAudioInputStream(decodedInputStream, decodedFormat);
                }
            }
        }
    }

    private void playAudioInputStream(AudioInputStream inputStream, AudioFormat format)
            throws IOException, LineUnavailableException {
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
            publisher.publishEvent(SoundPositionFinishEvent.INSTANCE);
        } finally {
            skipping = false;
        }
    }

    private static Optional<String> getTitle(InputStream inputStream)
            throws IOException, UnsupportedAudioFileException {
        Assert.isTrue(inputStream.markSupported(), "inputStream does not support mark.");
        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(inputStream);
        Object title = audioFileFormat.properties().get("title");
        return title == null ? Optional.empty() : Optional.of(title.toString());
    }

    @Override
    public void skip() {
        skipping = true;
    }
}
