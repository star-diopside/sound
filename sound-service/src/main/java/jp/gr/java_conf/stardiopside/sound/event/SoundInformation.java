package jp.gr.java_conf.stardiopside.sound.event;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class SoundInformation {

    private static final Logger logger = Logger.getLogger(SoundInformation.class.getName());
    private final AudioHeader audioHeader;
    private final Tag tag;

    public SoundInformation(Path path) throws Exception {
        var audioFile = AudioFileIO.read(path.toFile());
        audioHeader = audioFile.getAudioHeader();
        tag = audioFile.getTag();
    }

    public Optional<String> getTrack() {
        return optional(() -> tag.getFirst(FieldKey.TRACK));
    }

    public Optional<String> getTrackTotal() {
        return optional(() -> tag.getFirst(FieldKey.TRACK_TOTAL));
    }

    public Optional<String> getTitle() {
        return optional(() -> tag.getFirst(FieldKey.TITLE));
    }

    public Optional<String> getArtist() {
        return optional(() -> tag.getFirst(FieldKey.ARTIST));
    }

    public Optional<String> getDiscNo() {
        return optional(() -> tag.getFirst(FieldKey.DISC_NO));
    }

    public Optional<String> getDiscTotal() {
        return optional(() -> tag.getFirst(FieldKey.DISC_TOTAL));
    }

    public Optional<String> getAlbum() {
        return optional(() -> tag.getFirst(FieldKey.ALBUM));
    }

    public Optional<String> getAlbumArtist() {
        return optional(() -> tag.getFirst(FieldKey.ALBUM_ARTIST));
    }

    public OptionalInt getTrackLength() {
        return optional(audioHeader::getTrackLength);
    }

    public Optional<Duration> getTrackLengthAsDuration() {
        return getTrackLength().stream()
                .mapToObj(SoundInformation::convertTrackLengthToDuration)
                .findFirst();
    }

    public Optional<String> getFormat() {
        return optional(audioHeader::getFormat);
    }

    public Optional<String> getSampleRate() {
        return optional(audioHeader::getSampleRate);
    }

    public Optional<String> getBitRate() {
        return optional(audioHeader::getBitRate);
    }

    public Optional<String> getChannels() {
        return optional(audioHeader::getChannels);
    }

    public Map<String, String> toMap() {
        var info = new LinkedHashMap<String, String>();

        getTrack().ifPresent(s -> info.put("TRACK", s));
        getTrackTotal().ifPresent(s -> info.put("TRACK_TOTAL", s));
        getTitle().ifPresent(s -> info.put("TITLE", s));
        getArtist().ifPresent(s -> info.put("ARTIST", s));
        getDiscNo().ifPresent(s -> info.put("DISC_NO", s));
        getDiscTotal().ifPresent(s -> info.put("DISC_TOTAL", s));
        getAlbum().ifPresent(s -> info.put("ALBUM", s));
        getAlbumArtist().ifPresent(s -> info.put("ALBUM_ARTIST", s));
        getTrackLength().stream().mapToObj(i -> {
            var d = convertTrackLengthToDuration(i);
            return String.format("%d (%d:%02d)", i, d.toMinutes(), d.toSecondsPart());
        }).findFirst().ifPresent(s -> info.put("TRACK_LENGTH", s));
        getFormat().ifPresent(s -> info.put("FORMAT", s));
        getSampleRate().ifPresent(s -> info.put("SAMPLE_RATE", s));
        getBitRate().ifPresent(s -> info.put("BIT_RATE", s));
        getChannels().ifPresent(s -> info.put("CHANNELS", s));

        return info;
    }

    private static <T> Optional<T> optional(Supplier<T> s) {
        try {
            return Optional.ofNullable(s.get());
        } catch (Exception e) {
            logger.log(Level.FINE, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static OptionalInt optional(IntSupplier s) {
        try {
            return OptionalInt.of(s.getAsInt());
        } catch (Exception e) {
            logger.log(Level.FINE, e.getMessage(), e);
            return OptionalInt.empty();
        }
    }

    private static Duration convertTrackLengthToDuration(int trackLength) {
        return Duration.ofSeconds(trackLength);
    }
}
