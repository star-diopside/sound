package jp.gr.java_conf.stardiopside.sound.event;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundInformationEvent extends ApplicationEvent {

    public SoundInformationEvent(SoundInformation info) {
        super(info);
    }

    public SoundInformation getSoundInformation() {
        return (SoundInformation) getSource();
    }
}
