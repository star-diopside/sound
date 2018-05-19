package jp.gr.java_conf.stardiopside.sound.event;

import java.time.Duration;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundPositionEvent extends ApplicationEvent {

    public SoundPositionEvent(Duration position) {
        super(position);
    }

    public Duration getPosition() {
        return (Duration) super.getSource();
    }
}
