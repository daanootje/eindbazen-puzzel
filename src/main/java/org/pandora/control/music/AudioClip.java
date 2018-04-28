package org.pandora.control.music;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class AudioClip implements LineListener {

    private Clip clip;
    private String clipName;
    private Boolean audioCompleted;

    public AudioClip(Path path) {
        try {
            clipName = path.getFileName().toString();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(path.toFile());
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(audioStream);
            audioCompleted = false;
        } catch (UnsupportedAudioFileException e) {
            log.error(String.format("%s - Unsupported audio extension: %s", clipName, e.getMessage()));
        } catch (IOException e) {
            log.error(String.format("%s - IOException occurred when reading audio file: %s", clipName, e.getMessage()));
        } catch (LineUnavailableException e) {
            log.error(String.format("%s - LineUnavailableException occurred when reading audio file: %s", clipName, e.getMessage()));
        }
    }

    public void playAudio() {
        clip.start();
    }


    public void stopAudio() {
        clip.close();
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
        if (type == LineEvent.Type.START) {
            System.out.println("Playback started.");
        } else if (type == LineEvent.Type.STOP) {
            audioCompleted = true;
            System.out.println("Playback completed.");
        }
    }
}
