package jp.gr.java_conf.stardiopside.sound.internal;

import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.nio.file.Path;
import java.util.Optional;

public final class SoundInformations {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundInformations.class);
    private static final ConcurrentReferenceHashMap<Path, Optional<SoundInformation>> CACHE = new ConcurrentReferenceHashMap<>();

    private SoundInformations() {
    }

    public static Optional<SoundInformation> get(Path path) {
        try {
            return Optional.of(SoundInformation.read(path));
        } catch (Exception e) {
            LOGGER.warn("Cannot read sound information: " + path, e);
            return Optional.empty();
        }
    }

    public static Optional<SoundInformation> getFromCache(Path path) {
        return CACHE.computeIfAbsent(path, SoundInformations::get);
    }
}
