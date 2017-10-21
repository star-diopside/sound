package jp.gr.java_conf.star_diopside.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        App app = new App();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> future = executorService.submit(() -> app.play(Arrays.stream(args).map(Paths::get)));
        executorService.shutdown();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void play(Stream<Path> paths) {
        paths.forEach(path -> {
            try (Stream<Path> stream = Files.walk(path)) {
                stream.filter(Files::isRegularFile).forEach(file -> {
                    try (InputStream is = Files.newInputStream(file)) {
                        play(is, file.getFileName().toString());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public void play(InputStream is, String name) {
        logger.info("Begin " + name);
        try (AudioInputStream baseInputStream = AudioSystem
                .getAudioInputStream(is.markSupported() ? is : new BufferedInputStream(is))) {
            AudioFormat baseFormat = baseInputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                    baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            logger.info(baseFormat.getClass().toString() + " - " + baseFormat.toString());
            logger.info(decodedFormat.getClass().toString() + " - " + decodedFormat.toString());
            try (AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, baseInputStream);
                    SourceDataLine line = AudioSystem.getSourceDataLine(decodedFormat)) {
                line.addLineListener(event -> logger.info(event.toString()));
                line.open(decodedFormat);
                line.start();
                byte[] data = new byte[line.getBufferSize()];
                int size;
                while ((size = decodedInputStream.read(data, 0, data.length)) != -1) {
                    line.write(data, 0, size);
                }
                line.drain();
                line.stop();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        } finally {
            logger.info("End " + name);
        }
    }
}
