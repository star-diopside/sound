package jp.gr.java_conf.stardiopside.sound.service;

import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;

import javax.sound.sampled.LineListener;

public interface SoundPlayer {

    void setLineListener(LineListener lineListener);

    void setEventListener(Consumer<? super String> eventListener);

    void setExceptionListener(Consumer<? super Exception> exceptionListener);

    void setPositionListener(Consumer<? super Duration> positionListener);

    void terminate();

    void play();

    void skip();

    void stop();

    void back();

    void clear();

    void add(Path path);

}
