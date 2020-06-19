package jp.gr.java_conf.stardiopside.sound.event;

import java.nio.file.Path;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundInformationEvent extends ApplicationEvent {

    public SoundInformationEvent(Path path) throws Exception {
        super(new SoundInformation(path));
    }

    public SoundInformation getSoundInformation() {
        return (SoundInformation) getSource();
    }
}
