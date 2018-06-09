package org.pandora.control.serialcomm;

import gnu.io.SerialPortEvent;
import lombok.extern.slf4j.Slf4j;
import org.pandora.control.puzzle.PuzzleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SerialInterpreter extends SerialCommunicator {

    private PuzzleManager puzzleManager;

    @Autowired
    public SerialInterpreter(PuzzleManager puzzleManager) {
        super();
        this.puzzleManager = puzzleManager;
    }

    @Override
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            byte[] aReceiveBuffer = new byte[5];
            try {
                input.read(aReceiveBuffer,0,5);
                puzzleManager.getPuzzle(aReceiveBuffer[0])
                        .map(puzzle -> {
                            char c;
                            byte b;
                            StringBuilder s = new StringBuilder();
                            for(int i = 1; i < aReceiveBuffer.length; i++) {
                                b = aReceiveBuffer[i];
                                if(b != 0) {
                                    c = (char)b;
                                    if(c == '!') {
                                        break;
                                    } else {
                                        s.append(c);
                                    }
                                }
                            }

                            return false;
                        });
            } catch (IOException e) {
                log.error("Failed to read incoming data - %s", e.getMessage());
            }
        }
    }

}
