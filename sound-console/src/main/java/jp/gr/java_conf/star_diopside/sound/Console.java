package jp.gr.java_conf.star_diopside.sound;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import jp.gr.java_conf.star_diopside.sound.service.SoundService;

public class Console {

    private static final Logger logger = Logger.getLogger(Console.class.getName());

    public static void main(String[] args) {
        SoundService service = new SoundService();
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
