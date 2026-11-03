package jp.gr.java_conf.stardiopside.sound.event;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;

import java.time.Duration;
import java.util.Optional;

@SuppressWarnings("serial")
public class SoundPositionEvent extends ApplicationEvent {

    public SoundPositionEvent(@Nullable Duration position) {
        super(Optional.ofNullable(position));
    }

    @SuppressWarnings("unchecked")
    public Optional<Duration> getPosition() {
        return (Optional<Duration>) getSource();
    }
}
