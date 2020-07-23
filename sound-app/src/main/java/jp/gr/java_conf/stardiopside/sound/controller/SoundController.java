package jp.gr.java_conf.stardiopside.sound.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ResourceBundle;

import javax.annotation.PreDestroy;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformationEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import jp.gr.java_conf.stardiopside.sound.model.SoundData;
import jp.gr.java_conf.stardiopside.sound.service.SoundPlayer;
import jp.gr.java_conf.stardiopside.sound.util.PathStringConverter;

@Controller
public class SoundController implements Initializable {

    private final SoundPlayer player;
    private SoundData model = new SoundData();

    private Stage stage;
    private ChangeListener<Boolean> showingChangeListener;

    @FXML
    private TextField selectedFile;

    @FXML
    private Label trackPosition;

    @FXML
    private Label trackLength;

    @FXML
    private ProgressBar trackProgress;

    @FXML
    private ListView<Path> files;

    @FXML
    private ListView<String> history;

    public SoundController(SoundPlayer player) {
        this.player = player;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        showingChangeListener = (observable, oldValue, newValue) -> {
            if (oldValue.booleanValue() && !newValue.booleanValue()) {
                stop();
            }
        };
        stage.showingProperty().addListener(new WeakChangeListener<>(showingChangeListener));
        stage.titleProperty().bind(model.windowTitleValue());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedFile.textProperty().bindBidirectional(model.selectedFileProperty(), new PathStringConverter());
        trackPosition.textProperty().bind(Bindings.createStringBinding(() -> convertToString(model.getTrackPosition()),
                model.trackPositionProperty()));
        trackLength.textProperty().bind(Bindings.createStringBinding(() -> convertToString(model.getTrackLength()),
                model.trackLengthValue()));
        trackProgress.progressProperty().bind(model.trackProgressValue());
        files.itemsProperty().bind(model.filesProperty());
        history.itemsProperty().bind(model.historyProperty());
        player.play();
    }

    @PreDestroy
    public void onDestroy() {
        stop();
    }

    @EventListener
    public void onSoundInformationEvent(SoundInformationEvent event) {
        Platform.runLater(() -> {
            model.setSoundInformation(event.getSoundInformation());
            event.getSoundInformation().toMap().forEach((k, v) -> model.addHistory("\t" + k + ": " + v));
        });
    }

    @EventListener
    public void onSoundLineEvent(SoundLineEvent event) {
        Platform.runLater(() -> model.addHistory(event.getLineEvent()));
    }

    @EventListener
    public void onSoundActionEvent(SoundActionEvent event) {
        Platform.runLater(() -> model.addHistory(event.getSoundActionInformation().toString()));
    }

    @EventListener
    public void onSoundExceptionEvent(SoundExceptionEvent event) {
        Platform.runLater(() -> model.addHistory("Error: thrown " + event.getException().getClass().getName()));
    }

    @EventListener
    public void onSoundPositionEvent(SoundPositionEvent event) {
        Platform.runLater(() -> event.getPosition().ifPresentOrElse(
                model::setTrackPosition,
                () -> {
                    model.setSoundInformation(null);
                    model.setTrackPosition(null);
                }));
    }

    public void stop() {
        player.terminate();
    }

    @FXML
    private void onSelectDirectory(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();

        if (model.getSelectedFile() != null) {
            Path path = model.getSelectedFile().toAbsolutePath();
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            } else if (Files.isRegularFile(path)) {
                chooser.setInitialDirectory(path.getParent().toFile());
            }
        }

        File file = chooser.showDialog(stage);
        if (file != null) {
            model.setSelectedFile(file.toPath());
        }
    }

    @FXML
    private void onSelectFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();

        if (model.getSelectedFile() != null) {
            Path path = model.getSelectedFile().toAbsolutePath();
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            } else if (Files.isRegularFile(path)) {
                chooser.setInitialDirectory(path.getParent().toFile());
                chooser.setInitialFileName(path.getFileName().toString());
            }
        }

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            model.setSelectedFile(file.toPath());
        }
    }

    @FXML
    private void onFileAdd(ActionEvent event) {
        Path path = model.getSelectedFile();
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

    private static String convertToString(Duration d) {
        return d == null ? "00:00" : String.format("%02d:%02d", d.toMinutes(), d.toSecondsPart());
    }
}
