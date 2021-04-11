package jp.gr.java_conf.stardiopside.sound.model;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import jp.gr.java_conf.stardiopside.sound.service.SoundSource;

public class SoundFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundFile.class);
    private final ReadOnlyObjectWrapper<Path> fileName;
    private final ReadOnlyObjectWrapper<Path> directoryName;
    private final ReadOnlyStringWrapper extension;
    private final ReadOnlyStringWrapper audioInput = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper audioFileFormat = new ReadOnlyStringWrapper();

    public SoundFile(Path path) {
        var file = path.getFileName();
        var dir = path.toAbsolutePath().normalize().getParent();
        var ext = getExtension(file.toString());
        fileName = new ReadOnlyObjectWrapper<>(file);
        directoryName = new ReadOnlyObjectWrapper<>(dir);
        extension = new ReadOnlyStringWrapper(ext);

        var soundSource = SoundSource.of(path);

        try (var ais = soundSource.getAudioInputStream()) {
            audioInput.set(ais.toString());
        } catch (Exception e) {
            LOGGER.warn("Error occurred in " + soundSource, e);
            audioInput.set("Error");
        }

        try {
            audioFileFormat.set(soundSource.getAudioFileFormat().toString());
        } catch (Exception e) {
            LOGGER.warn("Error occurred in " + soundSource, e);
            audioFileFormat.set("Error");
        }
    }

    private static String getExtension(String file) {
        int index = file.lastIndexOf('.');
        return index == -1 ? "" : file.substring(index + 1);
    }

    public ReadOnlyObjectProperty<Path> fileNameProperty() {
        return fileName.getReadOnlyProperty();
    }

    public Path getFileName() {
        return fileName.get();
    }

    public ReadOnlyObjectProperty<Path> directoryNameProperty() {
        return directoryName.getReadOnlyProperty();
    }

    public Path getDirectoryName() {
        return directoryName.get();
    }

    public ReadOnlyStringProperty extensionProperty() {
        return extension.getReadOnlyProperty();
    }

    public String getExtension() {
        return extension.get();
    }

    public ReadOnlyStringProperty audioInputProperty() {
        return audioInput.getReadOnlyProperty();
    }

    public String getAudioInput() {
        return audioInput.get();
    }

    public ReadOnlyStringProperty audioFileFormatProperty() {
        return audioFileFormat.getReadOnlyProperty();
    }

    public String getAudioFileFormat() {
        return audioFileFormat.get();
    }
}
