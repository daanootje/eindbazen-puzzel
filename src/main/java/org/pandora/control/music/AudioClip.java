package org.pandora.control.music;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class AudioClip implements LineListener {

    private Clip clip;
    private String clipName;
    private Path path;
    private Boolean audioCompleted;

    AudioClip(Path path) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.path = path;
        clipName = path.getFileName().toString();
        initialize();
    }

    void playAudio() {
        if (!clip.isActive()) {
            clip.start();
        }
    }

    void pauseAudio() {
        clip.stop();
    }

    void restartAudio() {
        clip.stop();
        clip.close();
        try {
            initialize();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            log.error("Something went wrong restarting the audio");
        }
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();
        if (type == LineEvent.Type.START) {
            audioCompleted = false;
        } else if (type == LineEvent.Type.STOP) {
            audioCompleted = true;
        }
    }

    private void initialize() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(path.toFile());
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException e) {
            log.error(String.format("%s - Unsupported audio extension: %s", clipName, e.getMessage()));
            throw e;
        } catch (IOException e) {
            log.error(String.format("%s - IOException occurred when reading audio file: %s", clipName, e.getMessage()));
            throw e;
        } catch (LineUnavailableException e) {
            log.error(String.format("%s - LineUnavailableException occurred when reading audio file: %s", clipName, e.getMessage()));
            throw e;
        }
    }

}
