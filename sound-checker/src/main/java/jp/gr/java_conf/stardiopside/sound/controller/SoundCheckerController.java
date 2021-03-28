package jp.gr.java_conf.stardiopside.sound.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jp.gr.java_conf.stardiopside.sound.model.SoundCheckerData;
import jp.gr.java_conf.stardiopside.sound.model.SoundFile;

@Component
public class SoundCheckerController implements Initializable {

    private final SoundCheckerData model = new SoundCheckerData();
    private Path initialDirectory;
    private Stage stage;

    @FXML
    private TableView<SoundFile> soundFiles;

    @FXML
    private TableColumn<SoundFile, Path> soundFileName;

    @FXML
    private TableColumn<SoundFile, String> soundFileExtension;

    @FXML
    private TableColumn<SoundFile, Path> soundFileDirectory;

    @FXML
    private TableColumn<SoundFile, String> soundFileAudioInput;

    @FXML
    private TableColumn<SoundFile, String> soundFileAudioFormat;

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.titleProperty().bind(model.windowTitleProperty());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model.setWindowTitle(resources.getString("window.title"));
        soundFiles.itemsProperty().bind(model.soundFilesProperty());
        soundFileName.setCellValueFactory(param -> param.getValue().fileNameProperty());
        soundFileExtension.setCellValueFactory(param -> param.getValue().extensionProperty());
        soundFileDirectory.setCellValueFactory(param -> param.getValue().directoryNameProperty());
        soundFileAudioInput.setCellValueFactory(param -> param.getValue().audioInputProperty());
        soundFileAudioFormat.setCellValueFactory(param -> param.getValue().audioFileFormatProperty());
    }

    @FXML
    private void onSelectDirectory(ActionEvent event) {
        var chooser = new DirectoryChooser();

        getInitialDirectory().ifPresent(chooser::setInitialDirectory);
        var file = chooser.showDialog(stage);

        if (file != null) {
            Path path = file.toPath();
            model.addFiles(path);
            initialDirectory = path;
        }
    }

    @FXML
    private void onSelectFile(ActionEvent event) {
        var chooser = new FileChooser();

        getInitialDirectory().ifPresent(chooser::setInitialDirectory);
        var files = chooser.showOpenMultipleDialog(stage);

        if (files != null) {
            var paths = files.stream().map(File::toPath).toArray(Path[]::new);
            model.addFiles(paths);
            initialDirectory = Arrays.stream(paths).findFirst().map(Path::getParent).orElse(null);
        }
    }

    @FXML
    private void onClear(ActionEvent event) {
        model.clearSoundFiles();
    }

    private Optional<File> getInitialDirectory() {
        if (initialDirectory != null && Files.isDirectory(initialDirectory)) {
            return Optional.of(initialDirectory.toFile());
        } else {
            return Optional.empty();
        }
    }
}
