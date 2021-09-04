package jp.gr.java_conf.stardiopside.sound.event;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import javax.sound.sampled.AudioFormat;
import java.nio.file.Path;

@SuppressWarnings("serial")
public class SoundActionEvent extends ApplicationEvent {

    @Data
    private abstract static class AbstractSoundActionInformation<T> implements SoundActionInformation<T> {
        private final String name;
        private final T information;

        AbstractSoundActionInformation(String name, T information) {
            this.name = name;
            this.information = information;
        }

        @Override
        public String toString() {
            return name + ": " + toInformationString();
        }

        protected abstract String toInformationString();
    }

    private static class SimpleSoundActionInformation extends AbstractSoundActionInformation<String> {
        SimpleSoundActionInformation(String name, String information) {
            super(name, information);
        }

        @Override
        protected String toInformationString() {
            return getInformation();
        }
    }

    private static class SoundFileActionInformation extends AbstractSoundActionInformation<Path> {
        SoundFileActionInformation(String name, Path path) {
            super(name, path);
        }

        @Override
        protected String toInformationString() {
            var path = getInformation();
            return String.format("\"%s\" (Directory: \"%s\")", path.getFileName(),
                    path.toAbsolutePath().normalize().getParent());
        }
    }

    private static class SoundAudioFormatActionInformation extends AbstractSoundActionInformation<AudioFormat> {
        SoundAudioFormatActionInformation(String name, AudioFormat format) {
            super(name, format);
        }

        @Override
        protected String toInformationString() {
            var format = getInformation();
            return format.getClass() + " - " + format;
        }
    }

    public SoundActionEvent(String name, String information) {
        super(new SimpleSoundActionInformation(name, information));
    }

    public SoundActionEvent(String name, Path path) {
        super(new SoundFileActionInformation(name, path));
    }

    public SoundActionEvent(String name, AudioFormat format) {
        super(new SoundAudioFormatActionInformation(name, format));
    }

    public SoundActionInformation<?> getSoundActionInformation() {
        return (SoundActionInformation<?>) getSource();
    }
}
