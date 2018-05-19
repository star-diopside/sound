package jp.gr.java_conf.stardiopside.sound.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import jp.gr.java_conf.stardiopside.sound.model.SoundData;
import jp.gr.java_conf.stardiopside.sound.service.SoundPlayer;

@Controller
public class SoundController implements Initializable, DisposableBean {

    private final SoundPlayer player;
    private SoundData model = new SoundData();

    @FXML
    private TextField selectedFile;

    @FXML
    private Label status;

    @FXML
    private ListView<Path> files;

    @FXML
    private ListView<String> history;

    public SoundController(SoundPlayer player) {
        this.player = player;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedFile.textProperty().bindBidirectional(model.selectedFileProperty());
        status.textProperty().bind(model.statusProperty());
        files.setItems(model.getFiles());
        history.setItems(model.getHistory());
        player.play();
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @EventListener
    public void onSoundLineEvent(SoundLineEvent event) {
        Platform.runLater(() -> model.addHistory(event.getLineEvent()));
    }

    @EventListener
    public void onSoundActionEvent(SoundActionEvent event) {
        Platform.runLater(() -> model.addHistory(event.getAction()));
    }

    @EventListener
    public void onSoundExceptionEvent(SoundExceptionEvent event) {
        Platform.runLater(() -> model.addHistory("Error: thrown " + event.getException().getClass().getName()));
    }

    @EventListener
    public void onSoundPositionEvent(SoundPositionEvent event) {
        Platform.runLater(() -> model.setPosition(event.getPosition()));
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
