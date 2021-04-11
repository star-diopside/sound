package jp.gr.java_conf.stardiopside.sound.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jp.gr.java_conf.stardiopside.sound.util.Comparators;

public class SoundCheckerData {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundCheckerData.class);
    private final StringProperty windowTitle = new SimpleStringProperty();
    private final ReadOnlyObjectWrapper<ObservableList<SoundFile>> soundFiles = new ReadOnlyObjectWrapper<>(
            FXCollections.observableArrayList());

    public StringProperty windowTitleProperty() {
        return windowTitle;
    }

    public String getWindowTitle() {
        return windowTitle.get();
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle.set(windowTitle);
    }

    public ReadOnlyObjectProperty<ObservableList<SoundFile>> soundFilesProperty() {
        return soundFiles.getReadOnlyProperty();
    }

    public ObservableList<SoundFile> getSoundFiles() {
        return soundFiles.get();
    }

    public void addFiles(Path... paths) {
        addFiles(Arrays.stream(paths));
    }

    public void addFiles(Stream<Path> paths) {
        getSoundFiles().addAll(paths.flatMap(path -> {
            try {
                return Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile())
                        .sorted(Comparators.comparingPath());
            } catch (InvalidPathException | IOException e) {
                LOGGER.warn(e.getMessage(), e);
                return Stream.empty();
            }
        }).map(SoundFile::new).toArray(SoundFile[]::new));
    }

    public void clearSoundFiles() {
        getSoundFiles().clear();
    }
}
