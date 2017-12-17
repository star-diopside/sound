package jp.gr.java_conf.star_diopside.sound.model;

import java.nio.file.Path;
import java.time.Duration;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SoundData {

    private StringProperty selectedFile = new SimpleStringProperty();
    private StringProperty status = new SimpleStringProperty();
    private ObservableList<Path> files = FXCollections.observableArrayList();
    private ObservableList<String> history = FXCollections.observableArrayList();

    public StringProperty selectedFileProperty() {
        return selectedFile;
    }

    public String getSelectedFile() {
        return selectedFile.get();
    }

    public void setSelectedFile(String selectedFile) {
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

    public ObservableList<Path> getFiles() {
        return files;
    }

    public ObservableList<String> getHistory() {
        return history;
    }

    public void setPosition(Duration position) {
        status.set(position == null ? ""
                : String.format("%02d:%02d:%02d", position.toHours(), position.toMinutesPart(),
                        position.toSecondsPart()));
    }
}
