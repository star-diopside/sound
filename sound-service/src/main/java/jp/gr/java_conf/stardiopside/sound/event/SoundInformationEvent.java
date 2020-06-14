package jp.gr.java_conf.stardiopside.sound.event;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.Tag;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundInformationEvent extends ApplicationEvent {

    private AudioHeader audioHeader;
    private Tag tag;

    public SoundInformationEvent(AudioFile audioFile) {
        super(audioFile);
        this.audioHeader = audioFile.getAudioHeader();
        this.tag = audioFile.getTag();
    }

    public AudioHeader getAudioHeader() {
        return this.audioHeader;
    }

    public Tag getTag() {
        return this.tag;
    }
}
