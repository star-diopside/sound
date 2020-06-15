package jp.gr.java_conf.stardiopside.sound.event;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@SuppressWarnings("serial")
public class SoundInformationEvent extends ApplicationEvent {

    @Getter
    private Map<String, String> information;

    public SoundInformationEvent(AudioFile audioFile) {
        super(audioFile);
        var tag = audioFile.getTag();
        var info = new LinkedHashMap<String, String>();
        info.put("TRACK", tag.getFirst(FieldKey.TRACK));
        info.put("TRACK_TOTAL", tag.getFirst(FieldKey.TRACK_TOTAL));
        info.put("TITLE", tag.getFirst(FieldKey.TITLE));
        info.put("ARTIST", tag.getFirst(FieldKey.ARTIST));
        info.put("DISC_NO", tag.getFirst(FieldKey.DISC_NO));
        info.put("DISC_TOTAL", tag.getFirst(FieldKey.DISC_TOTAL));
        info.put("ALBUM", tag.getFirst(FieldKey.ALBUM));
        info.put("ALBUM_ARTIST", tag.getFirst(FieldKey.ALBUM_ARTIST));
        information = info;
    }
}
