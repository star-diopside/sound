package jp.gr.java_conf.stardiopside.sound.event;

import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;

import org.springframework.context.ApplicationEvent;

import lombok.Data;

@SuppressWarnings("serial")
public class SoundActionEvent extends ApplicationEvent {

    @Data
    private static class SimpleSoundActionInformation implements SoundActionInformation {
        private final String name;
        private final String information;

        public SimpleSoundActionInformation(String name, String information) {
            this.name = name;
            this.information = information;
        }

        @Override
        public String toString() {
            return name + ": " + information;
        }
    }

    private static class SoundFileActionInformation extends SimpleSoundActionInformation {
        public SoundFileActionInformation(String name, Path path) {
            super(name, String.format("\"%s\" (Directory: \"%s\")", path.getFileName(),
                    path.toAbsolutePath().normalize().getParent()));
        }
    }

    private static class SoundAudioFormatActionInformation extends SimpleSoundActionInformation {
        public SoundAudioFormatActionInformation(String name, AudioFormat format) {
            super(name, format.getClass() + " - " + format);
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

    public SoundActionInformation getSoundActionInformation() {
        return (SoundActionInformation) getSource();
    }
}
