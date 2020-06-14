package jp.gr.java_conf.stardiopside.sound.event;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundInformationEvent extends ApplicationEvent {

    private Map<String, Object> audioTags;

    public SoundInformationEvent(AudioFile audioFile) {
        super(audioFile);
        Tag tag = audioFile.getTag();
        audioTags = new LinkedHashMap<>();
        audioTags.put("TRACK", tag.getFields(FieldKey.TRACK));
        audioTags.put("TITLE", tag.getFields(FieldKey.TITLE));
        audioTags.put("ARTIST", tag.getFields(FieldKey.ARTIST));
        audioTags.put("DISC_NO", tag.getFields(FieldKey.DISC_NO));
        audioTags.put("ALBUM", tag.getFields(FieldKey.ALBUM));
        audioTags.put("ALBUM_ARTIST", tag.getFields(FieldKey.ALBUM_ARTIST));
    }

    public Map<String, Object> getAudioTags() {
        return audioTags;
    }
}
