package jp.gr.java_conf.stardiopside.sound.service;

public interface TaskExecutor {

    void start();

    void stop();

    void interrupt();

    void terminate();

    void add(Runnable task);

}
