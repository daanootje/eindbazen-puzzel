package org.pandora.control.puzzle;

import lombok.Builder;
import lombok.Data;
import org.pandora.control.music.AudioManager;

import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class Puzzle {

	private AudioManager audioManager;

	private String name;
	private String SL;
	private String SH;
	private Character identifier;
	private Map<String, Operation> PC_Puzzle;
	private Map<String, Operation> Puzzle_PC;
	private String puzzleState;
	private String stateInfo;

	@Data
	class Operation {
		private String name;
		private String type;
	}

	private synchronized void setPuzzleState(String puzzleState) {
		Puzzle_PC.values().stream()
				.filter(operation -> operation.getName().equals(puzzleState))
				.findAny()
				.ifPresent(operation -> this.puzzleState = puzzleState);
	}

	public synchronized String getPuzzleState() {
		return puzzleState;
	}

	private synchronized void setStateInfo(String stateInfo) {
		this.stateInfo = stateInfo;
	}

	public synchronized String getStateInfo() {
		return stateInfo;
	}

	public Optional<String> getPC_PuzzleCommand(String name) {
		return PC_Puzzle.entrySet().stream()
				.filter(entry -> entry.getValue().getName().equals(name) && entry.getValue().getType().equals("command"))
				.findAny()
				.map(Map.Entry::getKey);
	}

	public void apply(String message) {
		if(Puzzle_PC.containsKey(message)) {
			String name = Puzzle_PC.get(message).getName();
			String type = Puzzle_PC.get(message).getType();
			switch (type) {
				case "status":
					setPuzzleState(name);
					break;
				case "sound":
					audioManager.playMusic(name);
					break;
				case "info":
					setStateInfo(name);
					break;

			}
		}
	}

}
