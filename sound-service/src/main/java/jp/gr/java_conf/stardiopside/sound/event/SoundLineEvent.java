package jp.gr.java_conf.stardiopside.sound.event;

import javax.sound.sampled.LineEvent;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundLineEvent extends ApplicationEvent {

    public SoundLineEvent(LineEvent event) {
        super(event);
    }

    public LineEvent getLineEvent() {
        return (LineEvent) super.getSource();
    }
}
