package jp.gr.java_conf.star_diopside.sound.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.sound.sampled.LineListener;

public class SoundPlayer {

    private static final Logger logger = Logger.getLogger(SoundPlayer.class.getName());
    private static final Duration BACK_THRESHOLD = Duration.ofSeconds(2);
    private BlockingDeque<Path> beforeFiles = new LinkedBlockingDeque<>();
    private Deque<Path> afterFiles = new ConcurrentLinkedDeque<>();
    private volatile boolean operationWaiting;
    private volatile boolean stopping;
    private TaskExecutor taskExecutor = new TaskExecutor();
    private SoundService soundService = new SoundService();
    private Consumer<? super Exception> exceptionListener;

    public void setLineListener(LineListener lineListener) {
        soundService.setLineListener(lineListener);
    }

    public void setEventListener(Consumer<? super String> eventListener) {
        soundService.setEventListener(eventListener);
    }

    public void setExceptionListener(Consumer<? super Exception> exceptionListener) {
        soundService.setExceptionListener(exceptionListener);
        this.exceptionListener = exceptionListener;
    }

    public void setPositionListener(Consumer<? super Duration> positionListener) {
        soundService.setPositionListener(positionListener);
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            try {
                soundService.play(getPath());
            } catch (InterruptedException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            } catch (Exception e) {
                callListener(exceptionListener, e);
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                if (!stopping) {
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
        stopping = false;
        taskExecutor.add(new Task());
    }

    private Path getPath() throws InterruptedException {
        Path path = beforeFiles.takeFirst();
        afterFiles.addLast(path);
        return path;
    }

    private static <T> void callListener(Consumer<? super T> listener, T param) {
        if (listener != null) {
            listener.accept(param);
        }
    }

    public void skip() {
        soundService.skip();
        taskExecutor.interrupt();
    }

    public void stop() {
        stopping = true;
        skip();
    }

    public void back() {
        if (operationWaiting) {
            return;
        }
        operationWaiting = true;
        Duration nowPosition = soundService.getPosition();
        taskExecutor.add(() -> {
            Path path = afterFiles.pollLast();
            if (path != null) {
                beforeFiles.addFirst(path);
                if (nowPosition != null && nowPosition.compareTo(BACK_THRESHOLD) < 0) {
                    path = afterFiles.pollLast();
                    if (path != null) {
                        beforeFiles.addFirst(path);
                    }
                }
            }
            operationWaiting = false;
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
            callListener(exceptionListener, e);
            throw new UncheckedIOException(e);
        }
    }
}
