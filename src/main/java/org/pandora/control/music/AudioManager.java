package org.pandora.control.music;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class AudioManager {

    private Map<String, AudioClip> audioCollection = new HashMap<>();

    public AudioManager(String audioLocation) {
        try {
            Files.newDirectoryStream(Paths.get(audioLocation),
                    path -> path.toString().endsWith(".wav") || path.toString().endsWith(".mp4"))
                    .forEach(path -> audioCollection.put(removeExtension(path), new AudioClip(path)));
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

    public Set<String> getAudioList() {
        return audioCollection.keySet();
    }

    private static String removeExtension(Path path) {
        String fileName = path.getFileName().toString();
        int pos = fileName.lastIndexOf(".");
        if (pos == -1) {
            return fileName;
        }

        return fileName.substring(0, pos);
    }

}
