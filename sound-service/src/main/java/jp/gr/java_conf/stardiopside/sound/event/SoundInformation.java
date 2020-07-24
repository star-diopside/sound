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
import org.jaudiotagger.tag.FieldKey;

import lombok.Data;

@Data
public class SoundInformation {

    private static final Logger logger = Logger.getLogger(SoundInformation.class.getName());

    private final Optional<String> track;
    private final Optional<String> trackTotal;
    private final Optional<String> title;
    private final Optional<String> artist;
    private final Optional<String> discNo;
    private final Optional<String> discTotal;
    private final Optional<String> album;
    private final Optional<String> albumArtist;
    private final OptionalInt trackLength;
    private final Optional<Duration> trackLengthAsDuration;
    private final Optional<String> format;
    private final Optional<String> sampleRate;
    private final Optional<String> bitRate;
    private final Optional<String> channels;

    public SoundInformation(Path path) throws Exception {
        var audioFile = AudioFileIO.read(path.toFile());
        var audioHeader = audioFile.getAudioHeader();
        var tag = audioFile.getTag();

        track = optional(() -> tag.getFirst(FieldKey.TRACK));
        trackTotal = optional(() -> tag.getFirst(FieldKey.TRACK_TOTAL));
        title = optional(() -> tag.getFirst(FieldKey.TITLE));
        artist = optional(() -> tag.getFirst(FieldKey.ARTIST));
        discNo = optional(() -> tag.getFirst(FieldKey.DISC_NO));
        discTotal = optional(() -> tag.getFirst(FieldKey.DISC_TOTAL));
        album = optional(() -> tag.getFirst(FieldKey.ALBUM));
        albumArtist = optional(() -> tag.getFirst(FieldKey.ALBUM_ARTIST));
        trackLength = optional(audioHeader::getTrackLength);
        trackLengthAsDuration = trackLength.stream()
                .mapToObj(SoundInformation::convertTrackLengthToDuration)
                .findFirst();
        format = optional(audioHeader::getFormat);
        sampleRate = optional(audioHeader::getSampleRate);
        bitRate = optional(audioHeader::getBitRate);
        channels = optional(audioHeader::getChannels);
    }

    public Map<String, String> toMap() {
        var info = new LinkedHashMap<String, String>();

        track.ifPresent(s -> info.put("TRACK", s));
        trackTotal.ifPresent(s -> info.put("TRACK_TOTAL", s));
        title.ifPresent(s -> info.put("TITLE", s));
        artist.ifPresent(s -> info.put("ARTIST", s));
        discNo.ifPresent(s -> info.put("DISC_NO", s));
        discTotal.ifPresent(s -> info.put("DISC_TOTAL", s));
        album.ifPresent(s -> info.put("ALBUM", s));
        albumArtist.ifPresent(s -> info.put("ALBUM_ARTIST", s));
        trackLength.stream().mapToObj(i -> {
            var d = convertTrackLengthToDuration(i);
            return String.format("%d (%d:%02d)", i, d.toMinutes(), d.toSecondsPart());
        }).findFirst().ifPresent(s -> info.put("TRACK_LENGTH", s));
        format.ifPresent(s -> info.put("FORMAT", s));
        sampleRate.ifPresent(s -> info.put("SAMPLE_RATE", s));
        bitRate.ifPresent(s -> info.put("BIT_RATE", s));
        channels.ifPresent(s -> info.put("CHANNELS", s));

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
