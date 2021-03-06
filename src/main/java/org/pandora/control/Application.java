package org.pandora.control;

import org.glassfish.jersey.server.ResourceConfig;
import org.pandora.control.clock.CountDown;
import org.pandora.control.data.DataManager;
import org.pandora.control.display.DesktopApi;
import org.pandora.control.domain.Audio;
import org.pandora.control.domain.CORSFilter;
import org.pandora.control.domain.Display;
import org.pandora.control.domain.Hint;
import org.pandora.control.domain.Log;
import org.pandora.control.domain.Puzzle;
import org.pandora.control.domain.Room;
import org.pandora.control.domain.Time;
import org.pandora.control.domain.WebSocketConfig;
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

    @Value( "${log.location}" )
    private String logFolder;

    @Value( "${scripts.location}" )
    private String scriptsFolder;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ResourceConfig jerseyConfig() {
        return new ResourceConfig(Audio.class, Display.class, Hint.class, Log.class, Puzzle.class, Room.class, Time.class, WebSocketConfig.class, CORSFilter.class);
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
    public DataManager getDataManager() {
        return new DataManager(logFolder);
    }

    @Bean
    public PuzzleManager getPuzzleManager(AudioManager audioManager) throws IOException {
        return new PuzzleManager(audioManager, configFolder);
    }

    @Bean
    public DesktopApi getDesktopApi() {
        return new DesktopApi(scriptsFolder);
    }

}
