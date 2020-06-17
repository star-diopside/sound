package jp.gr.java_conf.stardiopside.sound.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class TaskExecutorImpl implements TaskExecutor {

    private static final Logger logger = Logger.getLogger(TaskExecutorImpl.class.getName());
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean running;
    private ExecutorService executorService;
    private Future<?> future;

    @PostConstruct
    public void onConstruct() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @PreDestroy
    public void onDestroy() {
        terminate();
    }

    @Override
    public void start() {
        running = true;
        if (future == null) {
            future = executorService.submit(new Task());
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void interrupt() {
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public void terminate() {
        stop();
        executorService.shutdown();
        interrupt();
    }

    @Override
    public void add(Runnable task) {
        taskQueue.add(task);
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            try {
                taskQueue.take().run();
            } catch (InterruptedException e) {
                logger.log(Level.FINE, e.getMessage(), e);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            } finally {
                if (running) {
                    future = executorService.submit(this);
                } else {
                    future = null;
                }
            }
        }
    }
}
