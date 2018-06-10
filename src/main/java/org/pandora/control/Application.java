package org.pandora.control;


import org.glassfish.jersey.server.ResourceConfig;
import org.pandora.control.clock.CountDown;
import org.pandora.control.domain.Time;
import org.pandora.control.music.AudioManager;
import org.pandora.control.puzzle.PuzzleManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@ComponentScan
@SpringBootApplication
public class Application {

    @Value( "${audio.folder.location}" )
    private String audioFolder;

    @Value( "${config.location}" )
    private String configFolder;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ResourceConfig jerseyConfig() {
        return new ResourceConfig(Time.class);
    }

    @Bean
    public CountDown getTime() {
        return new CountDown(3600);
    }

    @Bean
    public AudioManager getAudioManager() {
        return new AudioManager(audioFolder);
    }

    @Bean
    public PuzzleManager getPuzzleManager(AudioManager audioManager) throws IOException {
        return new PuzzleManager(audioManager, configFolder);
    }
}
