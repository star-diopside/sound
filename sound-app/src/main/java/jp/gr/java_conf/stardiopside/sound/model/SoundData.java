package jp.gr.java_conf.stardiopside.sound.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
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

    private final ObjectProperty<Path> selectedFile = new SimpleObjectProperty<>();
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

    private final ObjectBinding<Duration> trackLength = Bindings.createObjectBinding(
            () -> soundInformation.get() == null ? null
                    : soundInformation.get().getTrackLengthAsDuration().orElse(null),
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

    public ObjectProperty<Path> selectedFileProperty() {
        return selectedFile;
    }

    public Path getSelectedFile() {
        return selectedFile.get();
    }

    public void setSelectedFile(Path selectedFile) {
        this.selectedFile.set(selectedFile);
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

    public ObjectBinding<Duration> trackLengthBinding() {
        return trackLength;
    }

    public Duration getTrackLength() {
        return trackLength.get();
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
