package jp.gr.java_conf.stardiopside.sound.util;

import com.google.common.collect.Comparators;
import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.WeakHashMap;

public final class PathComparators {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathComparators.class);
    private static final WeakHashMap<Path, Optional<SoundInformation>> SOUND_INFORMATION_MAP = new WeakHashMap<>();

    private PathComparators() {
    }

    public static Comparator<Path> comparing() {
        return Comparator.comparing(PathParts::of, Comparator.naturalOrder());
    }

    private static record PathParts(Path parent, Path fileName) implements Comparable<PathParts> {

        private static PathParts of(Path path) {
            if (Files.isDirectory(path)) {
                return new PathParts(path, null);
            } else {
                return new PathParts(path.getParent(), path.getFileName());
            }
        }

        @Override
        public int compareTo(PathParts o) {
            int c = compareParent(o);
            return c != 0 ? c : compareFileName(o);
        }

        private int compareParent(PathParts o) {
            if (parent == null) {
                return o.parent == null ? 0 : -1;
            } else if (o.parent == null) {
                return 1;
            }

            int count = Math.min(parent.getNameCount(), o.parent.getNameCount());

            for (int i = 0; i < count; i++) {
                int c = parent.getName(i).compareTo(o.parent.getName(i));
                if (c != 0) {
                    return c;
                }
            }

            return parent.compareTo(o.parent);
        }

        private int compareFileName(PathParts o) {
            if (fileName == null) {
                return o.fileName == null ? 0 : -1;
            } else if (o.fileName == null) {
                return 1;
            } else {
                return fileName.compareTo(o.fileName);
            }
        }
    }

    public static Comparator<Path> comparingBySoundInformation() {
        return Comparator.comparing(path -> SOUND_INFORMATION_MAP.computeIfAbsent(path, p -> {
            try {
                return Optional.of(SoundInformation.read(p));
            } catch (Exception e) {
                LOGGER.warn("Cannot read sound information: " + p, e);
                return Optional.empty();
            }
        }), Comparators.emptiesLast(comparingSoundInformation()));
    }

    private static Comparator<SoundInformation> comparingSoundInformation() {
        return Comparator.comparing(SoundInformation::getAlbum, Comparators.emptiesLast(Comparator.naturalOrder()))
                .thenComparing(SoundInformation::getDiscNo, Comparators.emptiesLast(PathComparators::compareInt))
                .thenComparing(SoundInformation::getTrack, Comparators.emptiesLast(PathComparators::compareInt))
                .thenComparing(SoundInformation::getTitle, Comparators.emptiesLast(Comparator.naturalOrder()));
    }

    private static int compareInt(String s1, String s2) {
        return Integer.compare(NumberUtils.toInt(s1, Integer.MAX_VALUE), NumberUtils.toInt(s2, Integer.MAX_VALUE));
    }
}
