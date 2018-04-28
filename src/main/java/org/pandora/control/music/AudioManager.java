package org.pandora.control.music;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class AudioManager {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private Map<String, AudioClip> audioCollection = new HashMap<>();

    @Autowired
    public AudioManager(String audioLocation) {
        try {
            Files.newDirectoryStream(Paths.get(audioLocation),
                    path -> path.toString().endsWith(".wav") || path.toString().endsWith(".mp4"))
                    .forEach(path -> audioCollection.put(path.getFileName().toString(), new AudioClip(path)));
        } catch (IOException e) {
            log.error(String.format("IOException occurred when reading audio files: %s", e.getMessage()));
        }
    }

    public void playMusic(String name) {
        if (audioCollection.containsKey(name)) {
            audioCollection.get(name).playAudio();
        }
    }

    public void stopMusic(String name) {
        if (audioCollection.containsKey(name)) {
            audioCollection.get(name).stopAudio();
        }
    }

}
