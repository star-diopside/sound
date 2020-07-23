package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.context.ApplicationEventPublisher;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
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
        return play(new FileSoundSource(path));
    }

    @Override
    public boolean play(InputStream inputStream, String name) {
        return play(new InputStreamSoundSource(inputStream, name));
    }

    @Override
    public void skip() {
        skipping = true;
    }

    private boolean play(SoundSource soundSource) {
        soundSource.publishPlayBeginEvent(publisher);

        try {
            soundSource.publishSoundInformationEvent(publisher);
            outputAudioInformation(soundSource);
            try (AudioInputStream audioInputStream = soundSource.getAudioInputStream()) {
                playAudioInputStream(audioInputStream);
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            publishSoundExceptionEvent(e);
            return false;
        } finally {
            soundSource.publishPlayEndEvent(publisher);
        }

        return true;
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

    private void publishSoundExceptionEvent(Exception e) {
        logger.log(Level.WARNING, e.getMessage(), e);
        publisher.publishEvent(new SoundExceptionEvent(e));
    }

    private void outputAudioInformation(SoundSource soundSource) {
        try {
            soundSource.getAudioFileFormat().properties().forEach((k, v) -> logger.fine(k + " = " + v));
        } catch (UnsupportedAudioFileException | IOException e) {
            publishSoundExceptionEvent(e);
        }
    }
}
