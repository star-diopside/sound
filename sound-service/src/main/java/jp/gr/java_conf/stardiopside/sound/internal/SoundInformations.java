package jp.gr.java_conf.stardiopside.sound.internal;

import jp.gr.java_conf.stardiopside.sound.event.SoundInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.WeakHashMap;

public final class SoundInformations {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundInformations.class);
    private static final ThreadLocal<WeakHashMap<Path, Optional<SoundInformation>>> CACHE = ThreadLocal.withInitial(WeakHashMap::new);

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
        return CACHE.get().computeIfAbsent(path, SoundInformations::get);
    }
}
