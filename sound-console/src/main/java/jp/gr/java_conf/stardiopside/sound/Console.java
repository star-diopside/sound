package jp.gr.java_conf.stardiopside.sound;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.sound.sampled.LineEvent.Type;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformationEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import jp.gr.java_conf.stardiopside.sound.service.SoundService;

@SpringBootApplication
public class Console implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(Console.class.getName());
    private final SoundService service;
    private LocalDateTime start;
    private boolean stopped = false;
    private Optional<Duration> trackLength = Optional.empty();

    public Console(SoundService service) {
        this.service = service;
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = SpringApplication.run(Console.class, args)) {
        }
    }

    @Override
    public void run(String... args) {
        start = LocalDateTime.now();

        try {
            Arrays.stream(args).map(Paths::get).flatMap(path -> {
                try {
                    return Files.find(path, Integer.MAX_VALUE, (p, attr) -> attr.isRegularFile()).sorted();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).forEach(service::play);
        } finally {
            logger.info("Execution Time: " + getExecutionTimeString(start));
            stopped = true;
        }
    }

    @PreDestroy
    public void onDestroy() {
        if (start != null && !stopped) {
            System.err.println("Execution Time: " + getExecutionTimeString(start));
        }
    }

    private static String getExecutionTimeString(LocalDateTime start) {
        var end = LocalDateTime.now();
        var d = Duration.between(start, end);
        return String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }

    @EventListener
    public void onSoundInformationEvent(SoundInformationEvent event) {
        trackLength = event.getSoundInformation().getTrackLengthAsDuration();
        var info = event.getSoundInformation().toMap();
        info.keySet().stream()
                .mapToInt(String::length)
                .max()
                .ifPresent(i -> info.forEach(
                        (k, v) -> logger.info(String.format("%" + i + "s: %s", k, v))));
    }

    @EventListener
    public void onSoundLineEvent(SoundLineEvent event) {
        if (Type.STOP.equals(event.getLineEvent().getType())) {
            System.out.print("\r");
        }
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

    @EventListener
    public void onSoundPositionEvent(SoundPositionEvent event) {
        var position = event.getPosition();
        if (position == null) {
            trackLength = Optional.empty();
        } else {
            try (var formatter = new Formatter()) {
                formatter.format("\r---> %02d:%02d", position.toMinutes(), position.toSecondsPart());
                trackLength.ifPresent(d -> formatter.format(" / %02d:%02d (%3d%%)", d.toMinutes(), d.toSecondsPart(),
                        (int) (100.0 * position.getSeconds() / d.getSeconds())));
                System.out.print(formatter.toString());
            }
        }
    }
}
