package org.pandora.control.model;

import lombok.Value;

import java.util.EnumSet;

@Value
public enum Puzzles {
    PUZZLE1(EnumSet.allOf(Puzzles.PuzzleStates.class));

    private EnumSet<Puzzles.PuzzleStates> states;

    Puzzles(EnumSet<Puzzles.PuzzleStates> states) {
        this.states = states;
    }

    public enum PuzzleStates {
        INIT, IDLE, ACTIVE, FINISHED, HOLD
    }

}
