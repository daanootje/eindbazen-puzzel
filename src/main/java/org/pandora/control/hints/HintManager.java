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
        puzzleHints.putIfAbsent(puzzleName, 0);
        Integer count = puzzleHints.get(puzzleName) + 1;
        puzzleHints.put(puzzleName,count);
    }

    public Integer getNumberOfHints(String puzzleName) {
        return puzzleHints.getOrDefault(puzzleName, 0);
    }

}
