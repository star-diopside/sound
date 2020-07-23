package jp.gr.java_conf.stardiopside.sound.service;

import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.context.ApplicationEventPublisher;

interface SoundSource {

    AudioInputStream getAudioInputStream() throws UnsupportedAudioFileException, IOException;

    AudioFileFormat getAudioFileFormat() throws UnsupportedAudioFileException, IOException;

    default void publishPlayBeginEvent(ApplicationEventPublisher publisher) {
    }

    default void publishPlayEndEvent(ApplicationEventPublisher publisher) {
    }

    default void publishSoundInformationEvent(ApplicationEventPublisher publisher) {
    }
}
