package jp.gr.java_conf.stardiopside.sound.util;

import java.nio.file.Path;
import java.util.Comparator;

public final class Comparators {

    private Comparators() {
    }

    public static Comparator<Path> comparingPath() {
        return Comparators::comparePath;
    }

    private static int comparePath(Path a, Path b) {
        Path p1 = a.getParent();
        Path p2 = b.getParent();
        int count = Math.min(p1.getNameCount(), p2.getNameCount());

        for (int i = 0; i < count; i++) {
            int c = p1.getName(i).compareTo(p2.getName(i));
            if (c != 0) {
                return c;
            }
        }

        int c = p1.compareTo(p2);
        return c != 0 ? c : a.getFileName().compareTo(b.getFileName());
    }
}
