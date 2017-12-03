package jp.gr.java_conf.star_diopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundPlayer {

    private static final Logger logger = Logger.getLogger(SoundPlayer.class.getName());
    private BlockingQueue<Path> queue = new LinkedBlockingQueue<>();
    private AtomicBoolean stopping = new AtomicBoolean();
    Future<?> future;

    public void play() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            future = executorService.submit(() -> {
                while (!stopping.get()) {
                    try {
                        play(queue.take());
                    } catch (InterruptedException e) {
                        logger.log(Level.FINE, e.getMessage(), e);
                        break;
                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
                stopping.set(false);
            });
        } finally {
            executorService.shutdown();
        }
    }

    private void play(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            play(is, path.getFileName().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void play(InputStream is, String name) {
        logger.info("Begin " + name);
        try (AudioInputStream baseInputStream = AudioSystem
                .getAudioInputStream(is.markSupported() ? is : new BufferedInputStream(is))) {
            AudioFormat baseFormat = baseInputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                    baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            logger.info(baseFormat.getClass() + " - " + baseFormat);
            logger.info(decodedFormat.getClass() + " - " + decodedFormat);
            try (AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, baseInputStream);
                    SourceDataLine line = AudioSystem.getSourceDataLine(decodedFormat)) {
                line.addLineListener(event -> logger.info(event.toString()));
                line.open(decodedFormat);
                line.start();
                byte[] data = new byte[line.getBufferSize()];
                int size;
                while (!stopping.get() && (size = decodedInputStream.read(data, 0, data.length)) != -1) {
                    line.write(data, 0, size);
                }
                line.drain();
                line.stop();
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            logger.info("End " + name);
        }
    }

    public void stop() {
        stopping.set(true);
        future.cancel(true);
    }

    public void add(Path path) {
        try (Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).sorted()) {
            stream.forEach(queue::add);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
