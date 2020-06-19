package jp.gr.java_conf.stardiopside.sound.event;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class SoundInformation {

    private final AudioHeader audioHeader;
    private final Tag tag;

    public SoundInformation(Path path) throws Exception {
        var audioFile = AudioFileIO.read(path.toFile());
        audioHeader = audioFile.getAudioHeader();
        tag = audioFile.getTag();
    }

    public String getTrack() {
        return tag.getFirst(FieldKey.TRACK);
    }

    public String getTrackTotal() {
        return tag.getFirst(FieldKey.TRACK_TOTAL);
    }

    public String getTitle() {
        return tag.getFirst(FieldKey.TITLE);
    }

    public String getArtist() {
        return tag.getFirst(FieldKey.ARTIST);
    }

    public String getDiscNo() {
        return tag.getFirst(FieldKey.DISC_NO);
    }

    public String getDiscTotal() {
        return tag.getFirst(FieldKey.DISC_TOTAL);
    }

    public String getAlbum() {
        return tag.getFirst(FieldKey.ALBUM);
    }

    public String getAlbumArtist() {
        return tag.getFirst(FieldKey.ALBUM_ARTIST);
    }

    public int getTrackLength() {
        return audioHeader.getTrackLength();
    }

    public Duration getTrackLengthOfDuration() {
        return Duration.ofSeconds(getTrackLength());
    }

    public String getFormat() {
        return audioHeader.getFormat();
    }

    public String getSampleRate() {
        return audioHeader.getSampleRate();
    }

    public String getBitRate() {
        return audioHeader.getBitRate();
    }

    public String getChannels() {
        return audioHeader.getChannels();
    }

    public Map<String, String> toMap() {
        var info = new LinkedHashMap<String, String>();

        info.put("TRACK", getTrack());
        info.put("TRACK_TOTAL", getTrackTotal());
        info.put("TITLE", getTitle());
        info.put("ARTIST", getArtist());
        info.put("DISC_NO", getDiscNo());
        info.put("DISC_TOTAL", getDiscTotal());
        info.put("ALBUM", getAlbum());
        info.put("ALBUM_ARTIST", getAlbumArtist());
        info.put("TRACK_LENGTH", getTrackLengthString());
        info.put("FORMAT", getFormat());
        info.put("SAMPLE_RATE", getSampleRate());
        info.put("BIT_RATE", getBitRate());
        info.put("CHANNELS", getChannels());

        return info;
    }

    private String getTrackLengthString() {
        var d = getTrackLengthOfDuration();
        return String.format("%d (%d:%02d)", getTrackLength(), d.toMinutes(), d.toSecondsPart());
    }
}
