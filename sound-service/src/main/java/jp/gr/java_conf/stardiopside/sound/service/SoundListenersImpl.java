package jp.gr.java_conf.stardiopside.sound.service;

import java.time.Duration;
import java.util.function.Consumer;

import javax.sound.sampled.LineListener;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class SoundListenersImpl implements SoundListeners {

    private LineListener lineListener;
    private Consumer<? super String> eventListener;
    private Consumer<? super Exception> exceptionListener;
    private Consumer<? super Duration> positionListener;

}
