package jp.gr.java_conf.stardiopside.sound.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@SuppressWarnings("serial")
public class SoundExceptionEvent extends ApplicationEvent {

    @Getter
    private final Object source;

    public SoundExceptionEvent(Exception exception, Object source) {
        super(exception);
        this.source = source;
    }

    public Exception getException() {
        return (Exception) super.getSource();
    }
}
