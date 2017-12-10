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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private final Lock lock = new ReentrantLock();
    private BlockingDeque<Path> beforeFiles = new LinkedBlockingDeque<>();
    private Deque<Path> afterFiles = new ConcurrentLinkedDeque<>();
    private AtomicReference<Path> nowPlayingFile = new AtomicReference<>();
    private AtomicBoolean stopping = new AtomicBoolean();
    private AtomicBoolean skipping = new AtomicBoolean();
    private Future<?> future;

    public void play() {
        if (future != null) {
            return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            future = executorService.submit(() -> {
                stopping.set(false);
                while (!stopping.get()) {
                    try {
                        play(beforePlay());
                    } catch (InterruptedException e) {
                        logger.log(Level.FINE, e.getMessage(), e);
                        break;
                    } catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    } finally {
                        afterPlay();
                    }
                }
                future = null;
                stopping.set(false);
            });
        } finally {
            executorService.shutdown();
        }
    }

    private Path beforePlay() throws InterruptedException {
        lock.lock();
        try {
            Path path = beforeFiles.takeFirst();
            nowPlayingFile.set(path);
            return path;
        } finally {
            lock.unlock();
        }
    }

    private void afterPlay() {
        lock.lock();
        try {
            Path path = nowPlayingFile.getAndSet(null);
            if (path != null) {
                afterFiles.addLast(path);
            }
        } finally {
            lock.unlock();
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

    public void stop() {
        if (future != null) {
            stopping.set(true);
            skipping.set(true);
            future.cancel(true);
        }
    }

    public void skip() {
        skipping.set(true);
    }

    public void back() {
        lock.lock();
        try {
            Path path = afterFiles.pollLast();
            if (path != null) {
                beforeFiles.addFirst(nowPlayingFile.getAndSet(null));
                beforeFiles.addFirst(path);
                skipping.set(true);
            }
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            beforeFiles.clear();
            afterFiles.clear();
            nowPlayingFile.set(null);
            skipping.set(true);
        } finally {
            lock.unlock();
        }
    }

    public void add(Path path) {
        try (Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).sorted()) {
            stream.forEach(beforeFiles::addLast);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
