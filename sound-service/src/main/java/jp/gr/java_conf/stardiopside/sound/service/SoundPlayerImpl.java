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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SoundPlayerImpl implements SoundPlayer, InitializingBean, DisposableBean {

    private static final Logger logger = Logger.getLogger(SoundPlayerImpl.class.getName());
    private static final Duration BACK_THRESHOLD = Duration.ofSeconds(2);
    private BlockingDeque<Path> beforeFiles = new LinkedBlockingDeque<>();
    private Deque<Path> afterFiles = new ConcurrentLinkedDeque<>();
    private volatile boolean operationWaiting;
    private volatile boolean stopping;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private SoundService soundService;

    @Autowired
    private SoundListeners listeners;

    @Override
    public void afterPropertiesSet() throws Exception {
        taskExecutor.start();
    }

    @Override
    public void destroy() throws Exception {
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
                callListener(listeners.getExceptionListener(), e);
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                if (!stopping) {
                    taskExecutor.add(new Task());
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

    private static <T> void callListener(Consumer<? super T> listener, T param) {
        if (listener != null) {
            listener.accept(param);
        }
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
            callListener(listeners.getExceptionListener(), e);
            throw new UncheckedIOException(e);
        }
    }
}
