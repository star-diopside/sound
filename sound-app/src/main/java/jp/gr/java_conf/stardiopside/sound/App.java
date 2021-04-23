package jp.gr.java_conf.stardiopside.sound;

import java.util.ResourceBundle;

import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jp.gr.java_conf.stardiopside.sound.controller.SoundController;

@SpringBootApplication
public class App extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
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

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            LOGGER.error(e.getMessage(), e);
            var dialog = new ExceptionDialog(e);
            dialog.setHeaderText(messages.getString("message.uncaughtException"));
            dialog.show();
        });

        var loader = new FXMLLoader(getClass().getResource("sound.fxml"), messages);
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        SoundController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        SpringApplication.exit(applicationContext);
    }
}
