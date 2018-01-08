package jp.gr.java_conf.stardiopside.sound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jp.gr.java_conf.stardiopside.sound.controller.SoundController;

@SpringBootApplication
public class App extends Application {

    private ConfigurableApplicationContext applicationContext;
    private SoundController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        applicationContext = SpringApplication.run(App.class, getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
        controller = loader.getController();
    }

    @Override
    public void stop() throws Exception {
        controller.stop();
        applicationContext.close();
    }
}
