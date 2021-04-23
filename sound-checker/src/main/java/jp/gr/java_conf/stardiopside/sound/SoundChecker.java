package jp.gr.java_conf.stardiopside.sound;

import java.util.ResourceBundle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jp.gr.java_conf.stardiopside.sound.controller.SoundCheckerController;

@SpringBootApplication
public class SoundChecker extends Application {

    private ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        applicationContext = new SpringApplicationBuilder(getClass()).headless(false)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var messages = ResourceBundle.getBundle("messages");
        var loader = new FXMLLoader(getClass().getResource("sound-checker.fxml"), messages);
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        SoundCheckerController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        SpringApplication.exit(applicationContext);
    }
}
