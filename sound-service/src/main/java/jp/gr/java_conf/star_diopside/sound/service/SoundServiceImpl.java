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

import org.springframework.stereotype.Service;

@Service
public class SoundServiceImpl implements SoundService {

    private static final Logger logger = Logger.getLogger(SoundServiceImpl.class.getName());
    private volatile boolean skipping;
    private volatile Duration position;
    private LineListener lineListener;
    private Consumer<? super String> eventListener;
    private Consumer<? super Exception> exceptionListener;
    private Consumer<? super Duration> positionListener;

    @Override
    public Duration getPosition() {
        return position;
    }

    @Override
    public void setLineListener(LineListener lineListener) {
        this.lineListener = lineListener;
    }

    @Override
    public void setEventListener(Consumer<? super String> eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void setExceptionListener(Consumer<? super Exception> exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    @Override
    public void setPositionListener(Consumer<? super Duration> positionListener) {
        this.positionListener = positionListener;
    }

    @Override
    public void play(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            play(is, path.getFileName().toString());
        } catch (IOException e) {
            callListener(exceptionListener, e);
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void play(InputStream inputStream, String name) {
        String title = (name == null ? "untitled" : name);
        callListener(eventListener, "Begin " + title);
        try (AudioInputStream baseInputStream = AudioSystem.getAudioInputStream(
                inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream))) {
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
            callListener(eventListener, "End " + title);
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
                Duration pos = Duration.of(line.getMicrosecondPosition(), ChronoUnit.MICROS);
                position = pos;
                callListener(positionListener, pos);
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

    @Override
    public void skip() {
        skipping = true;
    }
}
