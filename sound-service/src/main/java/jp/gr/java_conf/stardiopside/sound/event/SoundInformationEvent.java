package jp.gr.java_conf.stardiopside.sound.event;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class SoundInformationEvent extends ApplicationEvent {

    private Map<String, Object> information;

    public SoundInformationEvent(AudioFile audioFile) {
        super(audioFile);
        Tag tag = audioFile.getTag();
        information = new LinkedHashMap<>();
        information.put("TRACK", tag.getFirst(FieldKey.TRACK));
        information.put("TRACK_TOTAL", tag.getFirst(FieldKey.TRACK_TOTAL));
        information.put("TITLE", tag.getFirst(FieldKey.TITLE));
        information.put("ARTIST", tag.getFirst(FieldKey.ARTIST));
        information.put("DISC_NO", tag.getFirst(FieldKey.DISC_NO));
        information.put("DISC_TOTAL", tag.getFirst(FieldKey.DISC_TOTAL));
        information.put("ALBUM", tag.getFirst(FieldKey.ALBUM));
        information.put("ALBUM_ARTIST", tag.getFirst(FieldKey.ALBUM_ARTIST));
    }

    public Map<String, Object> getInformation() {
        return information;
    }
}
