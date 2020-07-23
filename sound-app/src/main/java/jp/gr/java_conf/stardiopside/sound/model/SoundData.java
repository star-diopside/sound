package jp.gr.java_conf.stardiopside.sound.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;

public class SoundData {

    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    private ObjectProperty<Path> selectedFile = new SimpleObjectProperty<>();
    private ObjectProperty<SoundInformation> soundInformation = new SimpleObjectProperty<>();
    private ObjectProperty<Duration> trackPosition = new SimpleObjectProperty<>();
    private ReadOnlyObjectWrapper<ObservableList<Path>> files = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());
    private ReadOnlyObjectWrapper<ObservableList<String>> history = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());

    private ObservableStringValue windowTitle = Bindings.createStringBinding(
            () -> soundInformation.get() == null ? null
                    : Stream.of(soundInformation.get().getTitle(), soundInformation.get().getAlbum())
                            .flatMap(Optional::stream)
                            .filter(Predicate.not(String::isBlank))
                            .collect(Collectors.joining(" / ")),
            soundInformation);

    private ObservableObjectValue<Duration> trackLength = Bindings.createObjectBinding(
            () -> soundInformation.get() == null ? null
                    : soundInformation.get().getTrackLengthAsDuration().orElse(null),
            soundInformation);

    private ObservableDoubleValue trackProgress = Bindings.createDoubleBinding(
            () -> trackPosition.get() == null || trackLength.get() == null || trackLength.get().equals(Duration.ZERO)
                    ? 0.0
                    : (trackPosition.get().getSeconds() + trackPosition.get().getNano() * 1.0e-9)
                            / (trackLength.get().getSeconds() + trackLength.get().getNano() * 1.0e-9),
            trackPosition, trackLength);

    public ObservableStringValue windowTitleValue() {
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

    public ObservableObjectValue<Duration> trackLengthValue() {
        return trackLength;
    }

    public Duration getTrackLength() {
        return trackLength.get();
    }

    public ObservableDoubleValue trackProgressValue() {
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

    public ReadOnlyObjectProperty<ObservableList<String>> historyProperty() {
        return history.getReadOnlyProperty();
    }

    public ObservableList<String> getHistory() {
        return history.get();
    }

    public void addHistory(Object event) {
        getHistory().add(LocalDateTime.now().format(HISTORY_FORMATTER) + " - " + event);
    }
}
