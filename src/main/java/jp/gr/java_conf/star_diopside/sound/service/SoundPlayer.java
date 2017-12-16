package jp.gr.java_conf.star_diopside.sound.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
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
    private BlockingDeque<Path> beforeFiles = new LinkedBlockingDeque<>();
    private Deque<Path> afterFiles = new ConcurrentLinkedDeque<>();
    private AtomicBoolean stopping = new AtomicBoolean();
    private AtomicBoolean skipping = new AtomicBoolean();
    private TaskExecutor taskExecutor = new TaskExecutor();

    private class Task implements Runnable {
        @Override
        public void run() {
            try {
                play(getPath());
            } catch (InterruptedException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                if (!stopping.get()) {
                    taskExecutor.add(new Task());
                }
            }
        }
    }

    public SoundPlayer() {
        taskExecutor.start();
    }

    public void terminate() {
        stop();
        taskExecutor.terminate();
    }

    public void play() {
        stopping.set(false);
        taskExecutor.add(new Task());
    }

    private Path getPath() throws InterruptedException {
        Path path = beforeFiles.takeFirst();
        afterFiles.addLast(path);
        return path;
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
        skipping.set(false);
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
                while (!skipping.get() && (size = decodedInputStream.read(data, 0, data.length)) != -1) {
                    line.write(data, 0, size);
                }
                line.drain();
                line.stop();
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            skipping.set(false);
            logger.info("End " + name);
        }
    }

    public void skip() {
        skipping.set(true);
        taskExecutor.interrupt();
    }

    public void stop() {
        stopping.set(true);
        skip();
    }

    public void back() {
        taskExecutor.add(() -> {
            Path path = afterFiles.pollLast();
            if (path != null) {
                beforeFiles.addFirst(path);
            }
        });
        skip();
    }

    public void clear() {
        taskExecutor.add(() -> {
            beforeFiles.clear();
            afterFiles.clear();
        });
        skip();
    }

    public void add(Path path) {
        try (Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).sorted()) {
            stream.forEach(beforeFiles::addLast);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
