package jp.gr.java_conf.stardiopside.sound;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformationEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.service.SoundService;

@SpringBootApplication
public class Console implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(Console.class.getName());
    private final SoundService service;

    public Console(SoundService service) {
        this.service = service;
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = SpringApplication.run(Console.class, args)) {
        }
    }

    @Override
    public void run(String... args) {
        Arrays.stream(args).map(Paths::get).flatMap(path -> {
            try {
                return Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).sorted();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).forEach(service::play);
    }

    @EventListener
    public void onSoundInformationEvent(SoundInformationEvent event) {
        Map<String, Object> info = event.getInformation();
        info.keySet()
            .stream()
            .mapToInt(String::length)
            .max()
            .ifPresent(i ->
                info.forEach((k, v) ->
                    logger.info(String.format("%" + i + "s: %s", k, v))
                )
            );
    }

    @EventListener
    public void onSoundLineEvent(SoundLineEvent event) {
        logger.info(event.getLineEvent().toString());
    }

    @EventListener
    public void onSoundActionEvent(SoundActionEvent event) {
        logger.info(event.getAction());
    }

    @EventListener
    public void onSoundExceptionEvent(SoundExceptionEvent event) {
        logger.info("Error: thrown " + event.getException().getClass().getName());
    }
}
