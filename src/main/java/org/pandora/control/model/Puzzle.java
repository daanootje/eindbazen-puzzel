package org.pandora.control.model;

import lombok.Builder;
import lombok.Data;
import org.pandora.control.model.state.PuzzleState;
import org.pandora.control.music.AudioManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

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
	private PuzzleState puzzleState;
	private String stateInfo;

	@Autowired
	public Puzzle(AudioManager audioManager) {
		this.audioManager = audioManager;
		puzzleState = PuzzleState.INIT;
	}

	@Data
	public class Operation {
		private String name;
		private String type;
	}

	public synchronized void setPuzzleState(PuzzleState puzzleState) {
		this.puzzleState = puzzleState;
	}

	public synchronized PuzzleState getPuzzleState() {
		return puzzleState;
	}

	public synchronized void setStateInfo(String stateInfo) {
		this.stateInfo = stateInfo;
	}

	public synchronized String getStateInfo() {
		return stateInfo;
	}

	public void apply(String message) {
		Puzzle_PC.entrySet().stream()
				.filter(entry -> entry.getValue().getName().equals(message))
				.findFirst()
				.map(entry -> {
					String type = entry.getValue().getType();
					String key = entry.getKey();
					switch (type) {
						case "status":
							setPuzzleState(PuzzleState.valueOf(key.toUpperCase()));
							break;
						case "sound":
							audioManager.playMusic(key);
							break;
						case "info":
							setStateInfo(key);
							break;
					}
					return false;
				});
	}

}
