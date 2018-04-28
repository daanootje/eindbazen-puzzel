package org.pandora.control;

import org.junit.Test;
import org.pandora.control.music.AudioManager;

public class test {

    @Test
    public void test() throws Exception {
        AudioManager manager = new AudioManager("C:/Users/D.Rotman/Downloads/audio");
        manager.playMusic("cartoon001");

        Thread.sleep(5000);
    }

}
