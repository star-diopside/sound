package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionFinishEvent;
import lombok.Getter;

public class SoundServiceImpl implements SoundService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundServiceImpl.class);
    private volatile boolean skipping;

    @Getter
    private volatile Duration position;

    private final ApplicationEventPublisher publisher;

    public SoundServiceImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void skip() {
        skipping = true;
    }

    @Override
    public boolean play(SoundSource soundSource) {
        soundSource.publishPlayBeginEvent(publisher);

        try {
            soundSource.publishSoundInformationEvent(publisher);
            outputAudioInformation(soundSource);
            try (AudioInputStream audioInputStream = soundSource.getAudioInputStream()) {
                playAudioInputStream(audioInputStream);
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            publishSoundExceptionEvent(e, soundSource);
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

    private void publishSoundExceptionEvent(Exception e, SoundSource soundSource) {
        LOGGER.warn("Error occurred in " + soundSource, e);
        publisher.publishEvent(new SoundExceptionEvent(e, soundSource));
    }

    private void outputAudioInformation(SoundSource soundSource) {
        try {
            soundSource.getAudioFileFormat().properties().forEach((k, v) -> LOGGER.debug(k + " = " + v));
        } catch (UnsupportedAudioFileException | IOException e) {
            publishSoundExceptionEvent(e, soundSource);
        }
    }
}
