package jp.gr.java_conf.stardiopside.sound.event;

import java.time.Duration;

@SuppressWarnings("serial")
public class SoundFinishEvent extends SoundPositionEvent {

    public static final SoundFinishEvent INSTANCE = new SoundFinishEvent();

    private SoundFinishEvent() {
        super(Duration.ZERO);
    }

    @Override
    public Duration getPosition() {
        return null;
    }
}
