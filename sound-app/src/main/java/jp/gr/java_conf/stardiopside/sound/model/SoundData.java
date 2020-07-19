package jp.gr.java_conf.stardiopside.sound.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SoundData {

    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    private ObjectProperty<Path> selectedFile = new SimpleObjectProperty<>();
    private ObjectProperty<Duration> trackPosition = new SimpleObjectProperty<>();
    private ObjectProperty<Duration> trackLength = new SimpleObjectProperty<>();
    private ReadOnlyObjectWrapper<ObservableList<Path>> files = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());
    private ReadOnlyObjectWrapper<ObservableList<String>> history = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());

    private DoubleBinding trackProgress = Bindings.createDoubleBinding(() -> {
        if (trackPosition.get() == null || trackLength.get() == null) {
            return 0.0;
        } else {
            long length = trackLength.get().getSeconds();
            return length == 0L ? 0.0 : (double) trackPosition.get().getSeconds() / length;
        }
    }, trackPosition, trackLength);

    public ObjectProperty<Path> selectedFileProperty() {
        return selectedFile;
    }

    public Path getSelectedFile() {
        return selectedFile.get();
    }

    public void setSelectedFile(Path selectedFile) {
        this.selectedFile.set(selectedFile);
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

    public ObjectProperty<Duration> trackLengthProperty() {
        return trackLength;
    }

    public Duration getTrackLength() {
        return trackLength.get();
    }

    public void setTrackLength(Duration trackLength) {
        this.trackLength.set(trackLength);
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
