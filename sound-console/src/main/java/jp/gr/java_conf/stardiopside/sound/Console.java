package jp.gr.java_conf.stardiopside.sound;

import jakarta.annotation.PreDestroy;
import jp.gr.java_conf.stardiopside.sound.event.SoundActionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundExceptionEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformationEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundLineEvent;
import jp.gr.java_conf.stardiopside.sound.event.SoundPositionEvent;
import jp.gr.java_conf.stardiopside.sound.service.SoundService;
import jp.gr.java_conf.stardiopside.sound.service.SoundSource;
import jp.gr.java_conf.stardiopside.sound.util.PathComparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SpringBootApplication
public class Console implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Console.class);
    private final SoundService service;
    private LocalDateTime start;
    private boolean stopped = false;
    private Optional<Duration> trackLength = Optional.empty();

    public Console(SoundService service) {
        this.service = service;
    }

    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(Console.class, args));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        start = LocalDateTime.now();

        try {
            args.getNonOptionArgs().stream()
                    .flatMap(this::streamSoundSource)
                    .forEach(this::playSound);
        } finally {
            LOGGER.atInfo().setMessage("Execution Time: {}")
                    .addArgument(() -> getExecutionTimeString(start))
                    .log();
            stopped = true;
        }
    }

    @PreDestroy
    public void onDestroy() {
        if (start != null && !stopped) {
            System.err.println("Execution Time: " + getExecutionTimeString(start));
        }
    }

    private void playSound(Supplier<SoundSource> s) {
        try (var soundSource = s.get()) {
            try {
                service.play(soundSource);
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Error occurred in {}", soundSource);
            }
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log(e::getMessage);
        }
    }

    private Stream<Supplier<SoundSource>> streamSoundSource(String arg) {
        if (arg.startsWith("http://") || arg.startsWith("https://")) {
            return streamSoundSourceFromHttpScheme(arg);
        } else {
            return streamSoundSourceFromPath(arg);
        }
    }

    private Stream<Supplier<SoundSource>> streamSoundSourceFromHttpScheme(String arg) {
        try {
            var url = new URI(arg).toURL();
            return Stream.of(supplySoundSource(url));
        } catch (URISyntaxException | MalformedURLException e) {
            LOGGER.atWarn().setCause(e).log(e::getMessage);
            return Stream.empty();
        }
    }

    private Stream<Supplier<SoundSource>> streamSoundSourceFromPath(String arg) {
        try {
            return Files.find(Path.of(arg), Integer.MAX_VALUE, (_, attr) -> attr.isRegularFile())
                    .sorted(Comparator.comparing(Path::getParent, PathComparators.comparing())
                            .thenComparing(PathComparators.comparingBySoundInformation()))
                    .map(this::supplySoundSource);
        } catch (InvalidPathException | IOException e) {
            LOGGER.atWarn().setCause(e).log(e::getMessage);
            return Stream.empty();
        }
    }

    private Supplier<SoundSource> supplySoundSource(URL url) {
        return () -> {
            try {
                return SoundSource.of(url.openStream(), url.getFile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private Supplier<SoundSource> supplySoundSource(Path path) {
        return () -> SoundSource.of(path);
    }

    private String getExecutionTimeString(LocalDateTime start) {
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
                .ifPresent(i -> {
                    var keyFormat = "%" + i + "s";
                    info.forEach((k, v) -> LOGGER.atInfo().setMessage("{}: {}")
                            .addArgument(() -> String.format(keyFormat, k))
                            .addArgument(v)
                            .log());
                });
    }

    @EventListener
    public void onSoundLineEvent(SoundLineEvent event) {
        LOGGER.atInfo().log(() -> event.getLineEvent().toString());
    }

    @EventListener
    public void onSoundActionEvent(SoundActionEvent event) {
        LOGGER.atInfo().log(() -> event.getSoundActionInformation().toString());
    }

    @EventListener
    public void onSoundExceptionEvent(SoundExceptionEvent event) {
        LOGGER.atInfo().setMessage("Error: thrown {}")
                .addArgument(() -> event.getException().getClass().getName())
                .log();
    }

    @EventListener
    public void onSoundPositionEvent(SoundPositionEvent event) {
        event.getPosition().ifPresentOrElse(position -> {
            long len = trackLength.map(Duration::getSeconds).orElse(0L);
            int percent = (len == 0L ? 0 : (int) (100.0 * position.getSeconds() / len));
            int progress = Math.round(percent / 10.0F);

            try (var formatter = new Formatter()) {
                formatter.format("[%s%s] %02d:%02d", "=".repeat(progress), "-".repeat(10 - progress),
                        position.toMinutes(), position.toSecondsPart());
                trackLength.ifPresent(
                        d -> formatter.format(" / %02d:%02d (%3d%%)", d.toMinutes(), d.toSecondsPart(), percent));
                formatter.format("\r");
                System.out.print(formatter.toString());
            }
        }, () -> {
            trackLength = Optional.empty();
        });
    }
}
