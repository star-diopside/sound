package jp.gr.java_conf.stardiopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
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
        publisher.publishEvent(new SoundActionEvent("BEGIN", path));

        try {
            publishSoundInformationEvent(path);
            outputAudioInformation(path);
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(path.toFile())) {
                playAudioInputStream(audioInputStream);
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            publishSoundExceptionEvent(e);
            return false;
        } finally {
            publisher.publishEvent(new SoundActionEvent("END", path));
        }

        return true;
    }

    @Override
    public boolean play(InputStream inputStream, String name) {
        String title = (name == null ? "unnamed" : name);
        publisher.publishEvent(new SoundActionEvent("BEGIN", title));

        try (InputStream is = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream)) {
            outputAudioInformation(is);
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is)) {
                playAudioInputStream(audioInputStream);
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            publishSoundExceptionEvent(e);
            return false;
        } finally {
            publisher.publishEvent(new SoundActionEvent("END", title));
        }

        return true;
    }

    private void playAudioInputStream(AudioInputStream inputStream) throws IOException, LineUnavailableException {
        AudioFormat baseFormat = inputStream.getFormat();
        publisher.publishEvent(new SoundActionEvent("INPUT", baseFormat));
        if (baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                || baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
            playAudioInputStream(inputStream, baseFormat);
        } else {
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                    baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            publisher.publishEvent(new SoundActionEvent("DECODED", decodedFormat));
            try (AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, inputStream)) {
                playAudioInputStream(decodedInputStream, decodedFormat);
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

    private void publishSoundInformationEvent(Path path) {
        try {
            publisher.publishEvent(new SoundInformationEvent(path));
        } catch (Exception e) {
            publishSoundExceptionEvent(e);
        }
    }

    private void publishSoundExceptionEvent(Exception e) {
        logger.log(Level.WARNING, e.getMessage(), e);
        publisher.publishEvent(new SoundExceptionEvent(e));
    }

    private void outputAudioInformation(Path path) {
        try {
            outputAudioInformation(AudioSystem.getAudioFileFormat(path.toFile()));
        } catch (UnsupportedAudioFileException | IOException e) {
            publishSoundExceptionEvent(e);
        }
    }

    private void outputAudioInformation(InputStream inputStream) {
        Assert.isTrue(inputStream.markSupported(), "inputStream does not support mark.");
        try {
            outputAudioInformation(AudioSystem.getAudioFileFormat(inputStream));
        } catch (UnsupportedAudioFileException | IOException e) {
            publishSoundExceptionEvent(e);
        }
    }

    private static void outputAudioInformation(AudioFileFormat audioFileFormat) {
        audioFileFormat.properties().forEach((k, v) -> logger.fine(k + " = " + v));
    }

    @Override
    public void skip() {
        skipping = true;
    }
}
