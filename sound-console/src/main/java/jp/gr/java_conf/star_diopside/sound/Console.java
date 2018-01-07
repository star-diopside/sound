package jp.gr.java_conf.star_diopside.sound;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import jp.gr.java_conf.star_diopside.sound.service.SoundService;

@SpringBootApplication
public class Console implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(Console.class.getName());

    @Autowired
    private SoundService service;

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = SpringApplication.run(Console.class, args)) {
        }
    }

    @Override
    public void run(String... args) {
        service.setLineListener(event -> logger.info(event.toString()));
        service.setEventListener(logger::info);
        service.setExceptionListener(e -> logger.info("Error: thrown " + e.getClass().getName()));

        Arrays.stream(args).map(Paths::get).flatMap(path -> {
            try {
                return Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).sorted();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).forEach(service::play);
    }
}
