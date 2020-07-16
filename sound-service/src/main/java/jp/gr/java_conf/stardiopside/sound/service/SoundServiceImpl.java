package jp.gr.java_conf.stardiopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

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
            try (AudioInputStream audioInputStream = getAudioInputStream(path)) {
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
            try (AudioInputStream audioInputStream = getAudioInputStream(is)) {
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

    @Override
    public void skip() {
        skipping = true;
    }

    private void playAudioInputStream(AudioInputStream inputStream) throws IOException, LineUnavailableException {
        AudioFormat baseFormat = inputStream.getFormat();
        publisher.publishEvent(new SoundActionEvent("INPUT", baseFormat));
        if (isPlayableAudioFormat(baseFormat)) {
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

    private static boolean isPlayableAudioFormat(AudioFormat format) {
        return AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding()) && format.getSampleSizeInBits() == 16;
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
            outputAudioInformation(getAudioFileFormat(path));
        } catch (UnsupportedAudioFileException e) {
            publishSoundExceptionEvent(e);
        }
    }

    private void outputAudioInformation(InputStream inputStream) {
        Assert.isTrue(inputStream.markSupported(), "inputStream does not support mark.");
        try {
            outputAudioInformation(getAudioFileFormat(inputStream));
        } catch (UnsupportedAudioFileException e) {
            publishSoundExceptionEvent(e);
        }
    }

    private static void outputAudioInformation(AudioFileFormat audioFileFormat) {
        audioFileFormat.properties().forEach((k, v) -> logger.fine(k + " = " + v));
    }

    private static AudioInputStream getAudioInputStream(Path path) throws UnsupportedAudioFileException {
        for (AudioFileReader reader : ServiceLoader.load(AudioFileReader.class)) {
            try {
                return reader.getAudioInputStream(path.toFile());
            } catch (UnsupportedAudioFileException | IOException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
        }
        throw new UnsupportedAudioFileException("File of unsupported format");
    }

    private static AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException {
        Assert.isTrue(inputStream.markSupported(), "inputStream does not support mark.");
        for (AudioFileReader reader : ServiceLoader.load(AudioFileReader.class)) {
            try {
                return reader.getAudioInputStream(inputStream);
            } catch (UnsupportedAudioFileException | IOException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
        }
        throw new UnsupportedAudioFileException("Stream of unsupported format");
    }

    private static AudioFileFormat getAudioFileFormat(Path path) throws UnsupportedAudioFileException {
        for (AudioFileReader reader : ServiceLoader.load(AudioFileReader.class)) {
            try {
                return reader.getAudioFileFormat(path.toFile());
            } catch (UnsupportedAudioFileException | IOException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
        }
        throw new UnsupportedAudioFileException("File of unsupported format");
    }

    private static AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException {
        Assert.isTrue(inputStream.markSupported(), "inputStream does not support mark.");
        for (AudioFileReader reader : ServiceLoader.load(AudioFileReader.class)) {
            try {
                return reader.getAudioFileFormat(inputStream);
            } catch (UnsupportedAudioFileException | IOException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
        }
        throw new UnsupportedAudioFileException("Stream of unsupported format");
    }
}
