package jp.gr.java_conf.star_diopside.sound;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jp.gr.java_conf.star_diopside.sound.controller.SoundController;

public class App extends Application {

    private SoundController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
        controller = loader.getController();
    }

    @Override
    public void stop() throws Exception {
        controller.stop();
    }
}
