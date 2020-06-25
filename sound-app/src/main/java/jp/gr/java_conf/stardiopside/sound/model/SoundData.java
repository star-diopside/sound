package jp.gr.java_conf.stardiopside.sound.model;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SoundData {

    private static final DateTimeFormatter HISTORY_FORMATTER = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
    private ObjectProperty<Path> selectedFile = new SimpleObjectProperty<>();
    private StringProperty status = new SimpleStringProperty();
    private ReadOnlyObjectWrapper<ObservableList<Path>> files = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());
    private ReadOnlyObjectWrapper<ObservableList<String>> history = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());

    public ObjectProperty<Path> selectedFileProperty() {
        return selectedFile;
    }

    public Path getSelectedFile() {
        return selectedFile.get();
    }

    public void setSelectedFile(Path selectedFile) {
        this.selectedFile.set(selectedFile);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
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

    public void setPosition(Optional<Duration> position) {
        status.set(position.map(p -> String.format("%02d:%02d:%02d", p.toHours(), p.toMinutesPart(), p.toSecondsPart()))
                .orElse(""));
    }
}
