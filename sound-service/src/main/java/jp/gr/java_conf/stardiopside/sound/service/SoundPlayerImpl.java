package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.context.ApplicationEventPublisher;

import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;

public class SoundPlayerImpl implements SoundPlayer {

    private static final Logger logger = Logger.getLogger(SoundPlayerImpl.class.getName());
    private static final Duration BACK_THRESHOLD = Duration.ofSeconds(2);
    private BlockingDeque<Path> beforeFiles = new LinkedBlockingDeque<>();
    private Deque<Path> afterFiles = new ConcurrentLinkedDeque<>();
    private volatile boolean operationWaiting;
    private volatile boolean stopping;

    private final TaskExecutor taskExecutor;
    private final SoundService soundService;
    private final ApplicationEventPublisher publisher;

    public SoundPlayerImpl(TaskExecutor taskExecutor, SoundService soundService, ApplicationEventPublisher publisher) {
        this.taskExecutor = taskExecutor;
        this.soundService = soundService;
        this.publisher = publisher;
    }

    @PostConstruct
    public void onConstruct() {
        taskExecutor.start();
    }

    @PreDestroy
    public void onDestroy() {
        terminate();
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            try {
                Path path = beforeFiles.takeFirst();
                if (soundService.play(path)) {
                    afterFiles.addLast(path);
                }
            } catch (InterruptedException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            } catch (Exception e) {
                publisher.publishEvent(new SoundExceptionEvent(e));
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                if (!stopping) {
                    taskExecutor.add(this);
                }
            }
        }
    }

    @Override
    public void terminate() {
        stop();
        taskExecutor.terminate();
    }

    @Override
    public void play() {
        stopping = false;
        taskExecutor.add(new Task());
    }

    @Override
    public void skip() {
        soundService.skip();
        taskExecutor.interrupt();
    }

    @Override
    public void stop() {
        stopping = true;
        skip();
    }

    @Override
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

    @Override
    public void clear() {
        taskExecutor.add(() -> {
            beforeFiles.clear();
            afterFiles.clear();
        });
        skip();
    }

    @Override
    public void add(Path path) {
        try (Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).sorted()) {
            stream.forEach(beforeFiles::addLast);
        } catch (IOException e) {
            publisher.publishEvent(new SoundExceptionEvent(e));
            throw new UncheckedIOException(e);
        }
    }
}
