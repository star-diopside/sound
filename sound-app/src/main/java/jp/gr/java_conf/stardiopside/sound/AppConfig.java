package jp.gr.java_conf.stardiopside.sound;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.gr.java_conf.stardiopside.sound.service.SoundPlayer;
import jp.gr.java_conf.stardiopside.sound.service.SoundPlayerImpl;
import jp.gr.java_conf.stardiopside.sound.service.SoundService;
import jp.gr.java_conf.stardiopside.sound.service.SoundServiceImpl;
import jp.gr.java_conf.stardiopside.sound.service.TaskExecutor;
import jp.gr.java_conf.stardiopside.sound.service.TaskExecutorImpl;

@Configuration
public class AppConfig {

    private final ApplicationEventPublisher publisher;

    public AppConfig(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Bean
    public SoundPlayer soundPlayer() {
        return new SoundPlayerImpl(soundTaskExecutor(), soundService(), publisher);
    }

    @Bean
    public SoundService soundService() {
        return new SoundServiceImpl(publisher);
    }

    @Bean
    public TaskExecutor soundTaskExecutor() {
        return new TaskExecutorImpl();
    }
}
