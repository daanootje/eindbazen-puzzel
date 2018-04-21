package org.pandora.control.model;

import org.pandora.control.model.state.PuzzleState;

import lombok.Data;

@Data
public class Puzzle {
	
	private PuzzleState puzzleState;
	
	public Puzzle() {
		puzzleState = PuzzleState.INIT;
	}

}
