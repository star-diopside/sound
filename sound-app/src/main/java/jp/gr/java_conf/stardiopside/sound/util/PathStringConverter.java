package jp.gr.java_conf.stardiopside.sound.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.util.StringConverter;

public class PathStringConverter extends StringConverter<Path> {

    @Override
    public String toString(Path object) {
        return object == null ? null : object.toString();
    }

    @Override
    public Path fromString(String string) {
        return string == null ? null : Paths.get(string);
    }
}