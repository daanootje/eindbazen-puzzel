package org.pandora.control.model;

import lombok.Builder;
import lombok.Data;
import org.pandora.control.model.state.PuzzleState;
import org.pandora.control.serialcomm.SerialInterpreter;

import java.util.Map;

@Data
@Builder
public class Puzzle {

	private String name;
	private String SL;
	private String SH;
	private Character identifier;
	private Map<String, SerialInterpreter.Operation> PC_Puzzle;
	private Map<String, SerialInterpreter.Operation> Puzzle_PC;
	private PuzzleState puzzleState;

	public Puzzle() {
		puzzleState = PuzzleState.INIT;
	}

	public void

}
