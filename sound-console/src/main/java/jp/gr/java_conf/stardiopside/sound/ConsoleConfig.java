package jp.gr.java_conf.stardiopside.sound;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.gr.java_conf.stardiopside.sound.service.SoundService;
import jp.gr.java_conf.stardiopside.sound.service.SoundServiceImpl;

@Configuration
public class ConsoleConfig {

    @Bean
    public SoundService soundService(ApplicationEventPublisher publisher) {
        return new SoundServiceImpl(publisher);
    }
}
