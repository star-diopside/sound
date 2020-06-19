package jp.gr.java_conf.stardiopside.sound.event;

import org.jaudiotagger.audio.AudioFile;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundInformationEvent extends ApplicationEvent {

    public SoundInformationEvent(AudioFile audioFile) {
        super(new SoundInformation(audioFile));
    }

    public SoundInformation getSoundInformation() {
        return (SoundInformation) getSource();
    }
}
