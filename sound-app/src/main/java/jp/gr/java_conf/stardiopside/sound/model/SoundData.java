package jp.gr.java_conf.stardiopside.sound.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;

public class SoundData {

    private final ObjectProperty<SoundInformation> soundInformation = new SimpleObjectProperty<>();
    private final ObjectProperty<Duration> trackPosition = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<ObservableList<Path>> files = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());
    private final ReadOnlyObjectWrapper<ObservableList<History>> history = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());

    private final StringBinding windowTitle = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null
                    : Stream.of(soundInformation.get().getTitle(), soundInformation.get().getAlbum())
                            .flatMap(Optional::stream)
                            .filter(Predicate.not(String::isBlank))
                            .collect(Collectors.joining(" / ")),
            soundInformation);

    private final StringBinding track = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getTrack().orElse(null),
            soundInformation);

    private final StringBinding trackTotal = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getTrackTotal().orElse(null),
            soundInformation);

    private final StringBinding title = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getTitle().orElse(null),
            soundInformation);

    private final StringBinding artist = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getArtist().orElse(null),
            soundInformation);

    private final StringBinding discNo = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getDiscNo().orElse(null),
            soundInformation);

    private final StringBinding discTotal = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getDiscTotal().orElse(null),
            soundInformation);

    private final StringBinding album = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getAlbum().orElse(null),
            soundInformation);

    private final StringBinding albumArtist = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getAlbumArtist().orElse(null),
            soundInformation);

    private final ObjectBinding<OptionalInt> trackLengthInt = Bindings.createObjectBinding(
            () -> soundInformation.get() == null ? OptionalInt.empty() : soundInformation.get().getTrackLength(),
            soundInformation);

    private final ObjectBinding<Duration> trackLength = Bindings.createObjectBinding(
            () -> soundInformation.get() == null ? null
                    : soundInformation.get().getTrackLengthAsDuration().orElse(null),
            soundInformation);

    private final StringBinding format = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getFormat().orElse(null),
            soundInformation);

    private final StringBinding sampleRate = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getSampleRate().orElse(null),
            soundInformation);

    private final StringBinding bitRate = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getBitRate().orElse(null),
            soundInformation);

    private final StringBinding channels = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null : soundInformation.get().getChannels().orElse(null),
            soundInformation);

    private final DoubleBinding trackProgress = Bindings.createDoubleBinding(
            () -> trackPosition.get() == null || trackLength.get() == null || trackLength.get().equals(Duration.ZERO)
                    ? 0.0
                    : (trackPosition.get().getSeconds() + trackPosition.get().getNano() * 1.0e-9)
                            / (trackLength.get().getSeconds() + trackLength.get().getNano() * 1.0e-9),
            trackPosition, trackLength);

    public StringBinding windowTitleBinding() {
        return windowTitle;
    }

    public String getWindowTitle() {
        return windowTitle.get();
    }

    public ObjectProperty<SoundInformation> soundInformationProperty() {
        return soundInformation;
    }

    public SoundInformation getSoundInformation() {
        return soundInformation.get();
    }

    public void setSoundInformation(SoundInformation soundInformation) {
        this.soundInformation.set(soundInformation);
    }

    public ObjectProperty<Duration> trackPositionProperty() {
        return trackPosition;
    }

    public Duration getTrackPosition() {
        return trackPosition.get();
    }

    public void setTrackPosition(Duration trackPosition) {
        this.trackPosition.set(trackPosition);
    }

    public StringBinding trackBinding() {
        return track;
    }

    public String getTrack() {
        return track.get();
    }

    public StringBinding trackTotalBinding() {
        return trackTotal;
    }

    public String getTrackTotal() {
        return trackTotal.get();
    }

    public StringBinding titleBinding() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public StringBinding artistBinding() {
        return artist;
    }

    public String getArtist() {
        return artist.get();
    }

    public StringBinding discNoBinding() {
        return discNo;
    }

    public String getDiscNo() {
        return discNo.get();
    }

    public StringBinding discTotalBinding() {
        return discTotal;
    }

    public String getDiscTotal() {
        return discTotal.get();
    }

    public StringBinding albumBinding() {
        return album;
    }

    public String getAlbum() {
        return album.get();
    }

    public StringBinding albumArtistBinding() {
        return albumArtist;
    }

    public String getAlbumArtist() {
        return albumArtist.get();
    }

    public ObjectBinding<OptionalInt> trackLengthIntBinding() {
        return trackLengthInt;
    }

    public OptionalInt getTrackLengthInt() {
        return trackLengthInt.get();
    }

    public ObjectBinding<Duration> trackLengthBinding() {
        return trackLength;
    }

    public Duration getTrackLength() {
        return trackLength.get();
    }

    public StringBinding formatBinding() {
        return format;
    }

    public String getFormat() {
        return format.get();
    }

    public StringBinding sampleRateBinding() {
        return sampleRate;
    }

    public String getSampleRate() {
        return sampleRate.get();
    }

    public StringBinding bitRateBinding() {
        return bitRate;
    }

    public String getBitRate() {
        return bitRate.get();
    }

    public StringBinding channelsBinding() {
        return channels;
    }

    public String getChannels() {
        return channels.get();
    }

    public DoubleBinding trackProgressBinding() {
        return trackProgress;
    }

    public double getTrackProgress() {
        return trackProgress.get();
    }

    public ReadOnlyObjectProperty<ObservableList<Path>> filesProperty() {
        return files.getReadOnlyProperty();
    }

    public ObservableList<Path> getFiles() {
        return files.get();
    }

    public ReadOnlyObjectProperty<ObservableList<History>> historyProperty() {
        return history.getReadOnlyProperty();
    }

    public ObservableList<History> getHistory() {
        return history.get();
    }

    public void addHistory(Object event) {
        getHistory().add(new History(LocalDateTime.now(), String.valueOf(event)));
    }
}
