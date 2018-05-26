package jp.gr.java_conf.stardiopside.sound.event;

import java.time.Duration;

@SuppressWarnings("serial")
public class SoundPositionFinishEvent extends SoundPositionEvent {

    public static final SoundPositionFinishEvent INSTANCE = new SoundPositionFinishEvent();

    private SoundPositionFinishEvent() {
        super(Duration.ZERO);
    }

    @Override
    public Duration getPosition() {
        return null;
    }
}
