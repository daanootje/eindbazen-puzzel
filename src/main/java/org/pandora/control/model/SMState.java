package org.pandora.control.model;

import lombok.Value;
import java.util.EnumSet;


@Value
public enum SMState {
    INIT,
    IDLE,
    PUZZLESTATES(Puzzles.class),
    FINISHED;

    private Enum<Puzzles> puzzles;

    SMState() {
    }

    SMState(Enum<Puzzles> puzzles) {
        this.puzzles. = puzzles;
    }

    public Enum<Puzzles> getPuzzle(){
        return puzzles;
    }
}

