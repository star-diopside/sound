package jp.gr.java_conf.star_diopside.sound.model;

import java.nio.file.Path;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SoundData {

    private StringProperty selectedFile = new SimpleStringProperty();
    private ObservableList<Path> files = FXCollections.observableArrayList();

    public StringProperty selectedFileProperty() {
        return selectedFile;
    }

    public String getSelectedFile() {
        return selectedFile.get();
    }

    public void setSelectedFile(String selectedFile) {
        this.selectedFile.set(selectedFile);
    }

    public ObservableList<Path> getFiles() {
        return files;
    }
}
