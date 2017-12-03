package jp.gr.java_conf.star_diopside.sound.controller;

import java.io.File;
import java.net.URL;
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
        player.stop();
    }

    private Window getWindow() {
        return selectedFile.getScene().getWindow();
    }

    @FXML
    private void onSelectDirectory(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(getWindow());
        if (file != null) {
            model.setSelectedFile(file.toString());
        }
    }

    @FXML
    private void onSelectFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
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
}
