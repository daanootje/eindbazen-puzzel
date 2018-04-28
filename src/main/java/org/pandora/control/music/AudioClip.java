package org.pandora.control.music;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class AudioClip implements LineListener {

    private Clip clip;
    private String clipName;
    private Path path;
    private Boolean audioCompleted;

    public AudioClip(Path path) {
        this.path = path;
        clipName = path.getFileName().toString();
        initialize();
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
            audioCompleted = false;
        } else if (type == LineEvent.Type.STOP) {
            clip.close();
            initialize();
            audioCompleted = true;
        }
    }

    private void initialize() {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(path.toFile());
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException e) {
            log.error(String.format("%s - Unsupported audio extension: %s", clipName, e.getMessage()));
        } catch (IOException e) {
            log.error(String.format("%s - IOException occurred when reading audio file: %s", clipName, e.getMessage()));
        } catch (LineUnavailableException e) {
            log.error(String.format("%s - LineUnavailableException occurred when reading audio file: %s", clipName, e.getMessage()));
        }
    }

}
