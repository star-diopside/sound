package jp.gr.java_conf.stardiopside.sound;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
public class App extends Application {

    private static final Logger logger = Logger.getLogger(App.class.getName());
    private ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        applicationContext = SpringApplication.run(getClass(), getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var messages = ResourceBundle.getBundle("messages");

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            logger.log(Level.SEVERE, e.getMessage(), e);
            var dialog = new ExceptionDialog(e);
            dialog.setHeaderText(messages.getString("message.uncaughtException"));
            dialog.show();
        });

        var loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"), messages);
        loader.setControllerFactory(applicationContext::getBean);
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        applicationContext.close();
    }
}
