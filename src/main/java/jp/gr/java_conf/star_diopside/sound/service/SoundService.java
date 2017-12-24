package jp.gr.java_conf.star_diopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundService {

    private static final Logger logger = Logger.getLogger(SoundService.class.getName());
    private volatile boolean skipping;
    private volatile Duration position;
    private LineListener lineListener;
    private Consumer<? super String> eventListener;
    private Consumer<? super Exception> exceptionListener;
    private Consumer<? super Duration> positionListener;

    public Duration getPosition() {
        return position;
    }

    public void setLineListener(LineListener lineListener) {
        this.lineListener = lineListener;
    }

    public void setEventListener(Consumer<? super String> eventListener) {
        this.eventListener = eventListener;
    }

    public void setExceptionListener(Consumer<? super Exception> exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    public void setPositionListener(Consumer<? super Duration> positionListener) {
        this.positionListener = positionListener;
    }

    public void play(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            play(is, path.getFileName().toString());
        } catch (IOException e) {
            callListener(exceptionListener, e);
            throw new UncheckedIOException(e);
        }
    }

    private void play(InputStream is, String name) {
        callListener(eventListener, "Begin " + name);
        try (AudioInputStream baseInputStream = AudioSystem
                .getAudioInputStream(is.markSupported() ? is : new BufferedInputStream(is))) {
            AudioFormat baseFormat = baseInputStream.getFormat();
            callListener(eventListener, "INPUT: " + baseFormat.getClass() + " - " + baseFormat);
            if (baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                    || baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
                play(baseInputStream, baseFormat);
            } else {
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                        16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                callListener(eventListener, "DECODED: " + decodedFormat.getClass() + " - " + decodedFormat);
                try (AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat,
                        baseInputStream)) {
                    play(decodedInputStream, decodedFormat);
                }
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            callListener(exceptionListener, e);
        } finally {
            callListener(eventListener, "End " + name);
        }
    }

    private void play(AudioInputStream inputStream, AudioFormat format) throws IOException, LineUnavailableException {
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            skipping = false;
            if (lineListener != null) {
                line.addLineListener(lineListener);
            }
            line.open(format);
            line.start();
            byte[] data = new byte[line.getBufferSize()];
            int size;
            while (!skipping && (size = inputStream.read(data, 0, data.length)) != -1) {
                line.write(data, 0, size);
                position = Duration.of(line.getMicrosecondPosition(), ChronoUnit.MICROS);
                callListener(positionListener, position);
            }
            line.drain();
            line.stop();
            position = null;
            callListener(positionListener, null);
        } finally {
            skipping = false;
        }
    }

    private static <T> void callListener(Consumer<? super T> listener, T param) {
        if (listener != null) {
            listener.accept(param);
        }
    }

    public void skip() {
        skipping = true;
    }
}
