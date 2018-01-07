package jp.gr.java_conf.star_diopside.sound.service;

public interface TaskExecutor {

    void start();

    void stop();

    void interrupt();

    void terminate();

    void add(Runnable task);

}
