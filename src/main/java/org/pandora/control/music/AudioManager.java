package org.pandora.control.music;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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
                    .forEach(path -> {
                        String key = removeExtension(path);
                        try {
                            audioCollection.put(key, new AudioClip(path));
                        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                            audioCollection.remove(key);
                        }
                    });
        } catch (IOException e) {
            log.error(String.format("IOException occurred when reading audio files: %s", e.getMessage()));
        }
    }

    public void intializeAllMusic() {
        audioCollection.values().forEach(audioClip -> {
            audioClip.restartAudio();
            audioClip.pauseAudio();
        });
    }

    public void playMusic(String name) {
        if (audioCollection.containsKey(name)) {
            audioCollection.get(name).playAudio();
        }
    }

    public void pauseMusic(String name) {
        if (audioCollection.containsKey(name)) {
            audioCollection.get(name).pauseAudio();
        }
    }

    public void restartMusic(String name) {
        if (audioCollection.containsKey(name)) {
            audioCollection.get(name).restartAudio();
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
