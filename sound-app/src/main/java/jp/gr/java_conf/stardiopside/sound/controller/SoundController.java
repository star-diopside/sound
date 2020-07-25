package jp.gr.java_conf.stardiopside.sound.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformationEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import jp.gr.java_conf.stardiopside.sound.model.History;
import jp.gr.java_conf.stardiopside.sound.model.SoundData;
import jp.gr.java_conf.stardiopside.sound.service.SoundPlayer;

@Controller
public class SoundController implements Initializable {

    private final SoundPlayer player;
    private SoundData model = new SoundData();
    private Path initialDirectory;

    private Stage stage;
    private ChangeListener<Boolean> showingChangeListener;

    @FXML
    private Label trackPosition;

    @FXML
    private Label trackLength;

    @FXML
    private ProgressBar trackProgress;

    @FXML
    private TextField track;

    @FXML
    private TextField trackTotal;

    @FXML
    private TextField title;

    @FXML
    private TextField artist;

    @FXML
    private TextField discNo;

    @FXML
    private TextField discTotal;

    @FXML
    private TextField album;

    @FXML
    private TextField albumArtist;

    @FXML
    private TextField trackLengthInt;

    @FXML
    private TextField format;

    @FXML
    private TextField sampleRate;

    @FXML
    private TextField bitRate;

    @FXML
    private TextField channels;

    @FXML
    private ListView<Path> filesView;

    @FXML
    private TableView<History> historyView;

    @FXML
    private TableColumn<History, String> historyDateTimeColumn;

    @FXML
    private TableColumn<History, String> historyValueColumn;

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
        stage.titleProperty().bind(model.windowTitleBinding());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trackPosition.textProperty().bind(Bindings.createStringBinding(() -> convertToString(model.getTrackPosition()),
                model.trackPositionProperty()));
        trackLength.textProperty().bind(Bindings.createStringBinding(() -> convertToString(model.getTrackLength()),
                model.trackLengthBinding()));
        trackProgress.progressProperty().bind(model.trackProgressBinding());
        track.textProperty().bind(model.trackBinding());
        trackTotal.textProperty().bind(model.trackTotalBinding());
        title.textProperty().bind(model.titleBinding());
        artist.textProperty().bind(model.artistBinding());
        discNo.textProperty().bind(model.discNoBinding());
        discTotal.textProperty().bind(model.discTotalBinding());
        album.textProperty().bind(model.albumBinding());
        albumArtist.textProperty().bind(model.albumArtistBinding());
        trackLengthInt.textProperty().bind(Bindings.createStringBinding(
                () -> model.getTrackLengthInt().stream().mapToObj(String::valueOf).findFirst().orElse(null),
                model.trackLengthIntBinding()));
        format.textProperty().bind(model.formatBinding());
        sampleRate.textProperty().bind(model.sampleRateBinding());
        bitRate.textProperty().bind(model.bitRateBinding());
        channels.textProperty().bind(model.channelsBinding());
        filesView.itemsProperty().bind(model.filesProperty());
        historyView.itemsProperty().bind(model.historyProperty());
        historyDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTimeString"));
        historyValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        player.play();
    }

    @PreDestroy
    public void onDestroy() {
        stop();
    }

    @EventListener
    public void onSoundInformationEvent(SoundInformationEvent event) {
        Platform.runLater(() -> model.setSoundInformation(event.getSoundInformation()));
    }

    @EventListener
    public void onSoundLineEvent(SoundLineEvent event) {
        Platform.runLater(() -> model.addHistory(event.getLineEvent()));
    }

    @EventListener
    public void onSoundActionEvent(SoundActionEvent event) {
        Platform.runLater(() -> model.addHistory(event.getSoundActionInformation()));
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

        if (initialDirectory != null) {
            Path path = initialDirectory.toAbsolutePath();
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            } else if (Files.isRegularFile(path)) {
                chooser.setInitialDirectory(path.getParent().toFile());
            }
        }

        File file = chooser.showDialog(stage);
        if (file != null) {
            Path path = file.toPath();
            player.add(path);
            model.getFiles().add(path);
            initialDirectory = path;
        }
    }

    @FXML
    private void onSelectFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();

        if (initialDirectory != null) {
            Path path = initialDirectory.toAbsolutePath();
            if (Files.isDirectory(path)) {
                chooser.setInitialDirectory(path.toFile());
            } else if (Files.isRegularFile(path)) {
                chooser.setInitialDirectory(path.getParent().toFile());
                chooser.setInitialFileName(path.getFileName().toString());
            }
        }

        var files = chooser.showOpenMultipleDialog(stage);
        if (files != null) {
            var paths = files.stream().map(File::toPath).toArray(Path[]::new);
            player.addAll(paths);
            model.getFiles().addAll(paths);
            initialDirectory = Arrays.stream(paths).findFirst().map(Path::getParent).orElse(null);
        }
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
