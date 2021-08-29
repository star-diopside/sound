package jp.gr.java_conf.stardiopside.sound.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundExceptionEvent extends ApplicationEvent {

    @Getter
    private final Object causeSource;

    public SoundExceptionEvent(Exception exception, Object causeSource) {
        super(exception);
        this.causeSource = causeSource;
    }

    public Exception getException() {
        return (Exception) getSource();
    }
}
