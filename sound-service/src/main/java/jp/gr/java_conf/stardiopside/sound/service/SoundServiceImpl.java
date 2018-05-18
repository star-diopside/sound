package jp.gr.java_conf.stardiopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
public class SoundServiceImpl implements SoundService {

    private static final Logger logger = Logger.getLogger(SoundServiceImpl.class.getName());
    private volatile boolean skipping;

    @Getter
    private volatile Duration position;

    @Autowired
    private SoundListeners listeners;

    @Override
    public boolean play(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return play(is, path.getFileName().toString());
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            callListener(listeners.getExceptionListener(), e);
            return false;
        }
    }

    @Override
    public boolean play(InputStream inputStream, String name) {
        InputStream is = markSupportedInputStream(inputStream);
        String title = getTitle(is).orElse(name == null ? "untitled" : name);
        callListener(listeners.getEventListener(), "Begin " + title);
        try {
            return play(is);
        } finally {
            callListener(listeners.getEventListener(), "End " + title);
        }
    }

    private boolean play(InputStream inputStream) {
        try (AudioInputStream baseInputStream = AudioSystem
                .getAudioInputStream(markSupportedInputStream(inputStream))) {
            AudioFormat baseFormat = baseInputStream.getFormat();
            callListener(listeners.getEventListener(), "INPUT: " + baseFormat.getClass() + " - " + baseFormat);
            if (baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                    || baseFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
                play(baseInputStream, baseFormat);
            } else {
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                        16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                callListener(listeners.getEventListener(),
                        "DECODED: " + decodedFormat.getClass() + " - " + decodedFormat);
                try (AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat,
                        baseInputStream)) {
                    play(decodedInputStream, decodedFormat);
                }
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            callListener(listeners.getExceptionListener(), e);
            return false;
        }
        return true;
    }

    private void play(AudioInputStream inputStream, AudioFormat format) throws IOException, LineUnavailableException {
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            skipping = false;
            if (listeners.getLineListener() != null) {
                line.addLineListener(listeners.getLineListener());
            }
            line.open(format);
            line.start();
            byte[] data = new byte[line.getBufferSize()];
            int size;
            while (!skipping && (size = inputStream.read(data, 0, data.length)) != -1) {
                line.write(data, 0, size);
                Duration pos = Duration.of(line.getMicrosecondPosition(), ChronoUnit.MICROS);
                position = pos;
                callListener(listeners.getPositionListener(), pos);
            }
            line.drain();
            line.stop();
            position = null;
            callListener(listeners.getPositionListener(), null);
        } finally {
            skipping = false;
        }
    }

    private Optional<String> getTitle(InputStream inputStream) {
        try {
            AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(markSupportedInputStream(inputStream));
            Object title = audioFileFormat.properties().get("title");
            return title == null ? Optional.empty() : Optional.of(title.toString());
        } catch (IOException | UnsupportedAudioFileException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            callListener(listeners.getExceptionListener(), e);
            return Optional.empty();
        }
    }

    private static InputStream markSupportedInputStream(InputStream inputStream) {
        return inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
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
