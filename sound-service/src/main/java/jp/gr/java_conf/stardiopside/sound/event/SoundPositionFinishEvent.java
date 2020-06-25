package jp.gr.java_conf.stardiopside.sound.event;

@SuppressWarnings("serial")
public class SoundPositionFinishEvent extends SoundPositionEvent {

    public static final SoundPositionFinishEvent INSTANCE = new SoundPositionFinishEvent();

    private SoundPositionFinishEvent() {
        super(null);
    }
}
