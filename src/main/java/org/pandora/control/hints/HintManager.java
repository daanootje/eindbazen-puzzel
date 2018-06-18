package org.pandora.control.hints;

import org.pandora.control.display.DisplayManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HintManager {

    private Map<String,Integer> puzzleHints = new HashMap<>();

    private DisplayManager displayManager;

    @Autowired
    public HintManager(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

    public void displayHint(String puzzleName, String message) {
        puzzleHints.putIfAbsent(puzzleName, 0);
        Integer count = puzzleHints.get(puzzleName) + 1;
        puzzleHints.put(puzzleName,count);
//        displayMessage(message);
    }

    public Integer getNumberOfHints(String puzzleName) {
        return puzzleHints.getOrDefault(puzzleName, 0);
    }

}
