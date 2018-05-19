package jp.gr.java_conf.stardiopside.sound.event;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundActionEvent extends ApplicationEvent {

    public SoundActionEvent(String action) {
        super(action);
    }

    public String getAction() {
        return (String) super.getSource();
    }
}
