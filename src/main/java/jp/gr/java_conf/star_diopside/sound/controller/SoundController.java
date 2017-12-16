package jp.gr.java_conf.star_diopside.sound.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import jp.gr.java_conf.star_diopside.sound.model.SoundData;
import jp.gr.java_conf.star_diopside.sound.service.SoundPlayer;

public class SoundController implements Initializable {

    private SoundData model = new SoundData();
    private SoundPlayer player = new SoundPlayer();

    @FXML
    private TextField selectedFile;

    @FXML
    private ListView<Path> files;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedFile.textProperty().bindBidirectional(model.selectedFileProperty());
        files.setItems(model.getFiles());
        player.play();
    }

    public void stop() {
        player.terminate();
    }

    private Window getWindow() {
        return selectedFile.getScene().getWindow();
    }

    @FXML
    private void onSelectDirectory(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();

        if (model.getSelectedFile() != null) {
            Path path = Paths.get(model.getSelectedFile());
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            } else if (Files.isRegularFile(path)) {
                chooser.setInitialDirectory(path.getParent().toFile());
            }
        }

        File file = chooser.showDialog(getWindow());
        if (file != null) {
            model.setSelectedFile(file.toString());
        }
    }

    @FXML
    private void onSelectFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();

        if (model.getSelectedFile() != null) {
            Path path = Paths.get(model.getSelectedFile());
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            } else if (Files.isRegularFile(path)) {
                chooser.setInitialDirectory(path.getParent().toFile());
                chooser.setInitialFileName(path.getFileName().toString());
            }
        }

        File file = chooser.showOpenDialog(getWindow());
        if (file != null) {
            model.setSelectedFile(file.toString());
        }
    }

    @FXML
    private void onFileAdd(ActionEvent event) {
        Path path = Paths.get(model.getSelectedFile());
        player.add(path);
        model.getFiles().add(path);
    }

    @FXML
    private void onStart(ActionEvent event) {
        player.play();
    }

    @FXML
    private void onStop(ActionEvent event) {
        player.stop();
    }

    @FXML
    private void onBack(ActionEvent event) {
        player.back();
    }

    @FXML
    private void onSkip(ActionEvent event) {
        player.skip();
    }

    @FXML
    private void onClear(ActionEvent event) {
        player.clear();
        model.getFiles().clear();
    }
}
