package jp.gr.java_conf.stardiopside.sound.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;

import javax.sound.sampled.LineListener;

public interface SoundService {

    Duration getPosition();

    void setLineListener(LineListener lineListener);

    void setEventListener(Consumer<? super String> eventListener);

    void setExceptionListener(Consumer<? super Exception> exceptionListener);

    void setPositionListener(Consumer<? super Duration> positionListener);

    void play(Path path);

    void play(InputStream inputStream, String name);

    void skip();

}
