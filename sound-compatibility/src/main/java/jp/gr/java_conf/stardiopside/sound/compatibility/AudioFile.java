package jp.gr.java_conf.stardiopside.sound.compatibility;

import java.nio.file.Path;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class AudioFile {

    private final AudioHeader audioHeader;
    private final Tag tag;

    public AudioFile(Path path) throws Exception {
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
}
