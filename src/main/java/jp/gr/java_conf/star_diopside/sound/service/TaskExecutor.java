package jp.gr.java_conf.star_diopside.sound.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskExecutor {

    private static final Logger logger = Logger.getLogger(TaskExecutor.class.getName());
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean isRunning = new AtomicBoolean();
    private ExecutorService executorService;
    private Future<?> future;

    public TaskExecutor() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void start() {
        isRunning.set(true);
        if (future == null) {
            future = executorService.submit(new Task());
        }
    }

    public void stop() {
        isRunning.set(false);
    }

    public void interrupt() {
        if (future != null) {
            future.cancel(true);
        }
    }

    public void terminate() {
        stop();
        executorService.shutdown();
        interrupt();
    }

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
                if (isRunning.get()) {
                    future = executorService.submit(new Task());
                } else {
                    future = null;
                }
            }
        }
    }
}
