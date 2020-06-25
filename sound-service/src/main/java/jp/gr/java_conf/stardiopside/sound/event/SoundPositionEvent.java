package jp.gr.java_conf.stardiopside.sound.event;

import java.time.Duration;
import java.util.Optional;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundPositionEvent extends ApplicationEvent {

    public SoundPositionEvent(Duration position) {
        super(Optional.ofNullable(position));
    }

    @SuppressWarnings("unchecked")
    public Optional<Duration> getPosition() {
        return (Optional<Duration>) getSource();
    }
}
