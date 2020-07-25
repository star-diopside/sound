package jp.gr.java_conf.stardiopside.sound.service;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

public interface SoundPlayer {

    void terminate();

    void play();

    void skip();

    void stop();

    void back();

    void clear();

    void add(Path path);

    default void addAll(Path... paths) {
        Arrays.stream(paths).forEach(this::add);
    }

    default void addAll(Collection<Path> paths) {
        paths.stream().forEach(this::add);
    }
}
