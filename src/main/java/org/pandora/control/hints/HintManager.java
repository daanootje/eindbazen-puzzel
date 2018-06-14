package org.pandora.control.hints;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HintManager {

    private Map<String,Integer> puzzleHints = new HashMap<>();

    public HintManager() {

    }

    public void displayHint(String puzzleName) {
        if(!puzzleHints.containsKey(puzzleName)) {
            puzzleHints.put(puzzleName,0);
        }

    }

    public Integer getNumberOfHints(String puzzleName) {
        return puzzleHints.getOrDefault(puzzleName, 0);
    }

}
