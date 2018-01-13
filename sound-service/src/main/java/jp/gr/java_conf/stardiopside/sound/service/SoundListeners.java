package jp.gr.java_conf.stardiopside.sound.service;

import java.time.Duration;
import java.util.function.Consumer;

import javax.sound.sampled.LineListener;

public interface SoundListeners {

    LineListener getLineListener();

    void setLineListener(LineListener lineListener);

    Consumer<? super String> getEventListener();

    void setEventListener(Consumer<? super String> eventListener);

    Consumer<? super Exception> getExceptionListener();

    void setExceptionListener(Consumer<? super Exception> exceptionListener);

    Consumer<? super Duration> getPositionListener();

    void setPositionListener(Consumer<? super Duration> positionListener);

}
