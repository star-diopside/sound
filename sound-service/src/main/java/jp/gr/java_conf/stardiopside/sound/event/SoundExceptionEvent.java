package jp.gr.java_conf.stardiopside.sound.event;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundExceptionEvent extends ApplicationEvent {

    public SoundExceptionEvent(Exception exception) {
        super(exception);
    }

    public Exception getException() {
        return (Exception) super.getSource();
    }
}
