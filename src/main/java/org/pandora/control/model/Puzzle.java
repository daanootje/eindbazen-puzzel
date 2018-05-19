package org.pandora.control.model;

import org.pandora.control.model.state.PuzzleState;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Puzzle {

	private String name;
	private String SL;
	private String SH;
	private List<Map<String, Operation>> PC_Puzzle;
	private List<Map<String, Operation>> Puzzle_PC;
	private PuzzleState puzzleState;

	public Puzzle() {
		puzzleState = PuzzleState.INIT;
	}

	private class Operation {
		private String payload;
		private String type;
	}
}
