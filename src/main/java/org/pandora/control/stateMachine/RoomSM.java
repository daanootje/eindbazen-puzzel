package org.pandora.control.stateMachine;

import gnu.io.SerialPortEvent;
import lombok.extern.slf4j.Slf4j;
import org.pandora.control.clock.CountDown;
import org.pandora.control.data.PuzzleData;
import org.pandora.control.hints.HintManager;
import org.pandora.control.music.AudioManager;
import org.pandora.control.puzzle.Puzzle;
import org.pandora.control.puzzle.PuzzleManager;
import org.pandora.control.serialcomm.SerialCommunicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class RoomSM extends SerialCommunicator {

	@Autowired
	private StateMachine<String,String> stateMachine;

	private ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
	private PuzzleManager puzzleManager;
	private AudioManager audioManager;
	private CountDown timeRemaining;
	private HintManager hintManager;

	private Boolean finished = false;
	private Boolean started = false;
	private Boolean reset = true;

	private Map<String, PuzzleData> puzzlesData = new HashMap<>();
	private Integer duration;
	private Boolean succeeded;

	@Autowired
	public RoomSM(CountDown timeRemaining, AudioManager audioManager, PuzzleManager puzzleManager, HintManager hintManager) {
		super();
		this.timeRemaining = timeRemaining;
		this.puzzleManager = puzzleManager;
		this.audioManager = audioManager;
		this.hintManager = hintManager;
	}

	public Map<String, PuzzleData> getPuzzleData() {
		return puzzlesData;
	}

	public Boolean getSucceeded() {
		return succeeded;
	}

	public Boolean getFinished() {
		return finished;
	}

	public void resetSM() {
		try {
			stopSM();
			EXECUTOR = Executors.newFixedThreadPool(10);
			checkForSerialPorts();
			stateMachine.stop();
			stateMachine.start();
			reset = true;
		} catch (Exception e) {
			log.error(String.format("Something went wrong initializing statemachine - %s", e.getMessage()));
		}
	}

	public void startPuzzles() {
		if(started) {
			stateMachine.sendEvent("START");
		}
	}

	public void startSM(String port) {
		if(reset) {
			if(isConnected()) {
				try {
					initializePort(port);
					stateMachine.start();
					initPuzzles();
					checkInitPuzzles().get(15, TimeUnit.MINUTES);
					finished = false;
					started = true;
					reset = false;
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					log.error(String.format("Something went wrong starting statemachine - %s", e.getMessage()));
				}
			} else {
				log.info("Serial port is not connected");
			}
		}
	}

	public void stopSM() {
		stateMachine.stop();
		started = false;
		succeeded = false;
	}

	public void finishSM() {
		finishPuzzles();
		stateMachine.stop();
		succeeded = false;
		endSM();
	}

	public String getState() {
		return stateMachine.getState().getId();
	}

	public void setPuzzleState(String puzzleName, String state) {
		sendCommandToPuzzle(puzzleName, state);
		if (state.equals("finish") && puzzleManager.getPuzzle(puzzleName).isPresent()) {
			puzzlesData.put(puzzleName, PuzzleData.builder().succeeded(false).build());
		}
	}

	private void finishPuzzle(String puzzleName) {
		PuzzleData.PuzzleDataBuilder puzzleData = PuzzleData.builder()
				.name(puzzleName)
				.duration(duration)
				.hints(hintManager.getNumberOfHints(puzzleName));
		duration = timeRemaining.getElapsedTime() - duration; asd is not right with splitting states //TODO finish puzzle when from serial
		if (!puzzlesData.containsKey(puzzleName)) {
			puzzleData.succeeded(true);
		}
		puzzlesData.put(puzzleName, puzzleData.build());
	}

	private Action<String, String> finalizeSM() {
		succeeded = true;
		return endSM();
	}

	private Action<String, String> endSM() {
		return ctx -> {
			log.info("Finalizing room");
			EXECUTOR.shutdownNow();
			log.info("Stop all audio");
			audioManager.intializeAllMusic();
			log.info("Disconnecting serial port");
			disconnect();
			log.info("Stop timer");
			timeRemaining.pause();
			finished = true;
			started = false;
		};
	}

	private Action<String, String> startRoom() {
		return ctx -> {
			log.info("Starting timer");
			timeRemaining.start();
			log.info("Starting audio");
			audioManager.playMusic("backgroundMusic");
		};
	}

	@Override
	public void serialEvent(SerialPortEvent evt) {
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			byte[] aReceiveBuffer = new byte[5];
			try {
				getInput().read(aReceiveBuffer,0,5);
				byte id = aReceiveBuffer[0];
				char c;
				byte b;
				StringBuilder s = new StringBuilder();
				for(int i = 1; i < aReceiveBuffer.length; i++) {
					b = aReceiveBuffer[i];
					if(b != 0) {
						c = (char)b;
						if(c == '!') {
							break;
						} else {
							s.append(c);
						}
					}
				}
				applyPuzzle_PC(id, s.toString());
			} catch (IOException e) {
				log.error("Failed to read incoming data - %s", e.getMessage());
			}
		}
	}

	private Future applyPuzzle_PC(byte identifier, String message) {
		return EXECUTOR.submit(() -> {
			Optional<Puzzle> puzzle = puzzleManager.getPuzzle(identifier);
			if (puzzle.isPresent()) {
				puzzle.get().apply(message);
			} else {
				log.info(String.format("Could not identify serial event to a puzzle: %s", identifier));
			}
		});
	}

	private Future initPuzzles() {
		return EXECUTOR.submit(() ->sendCommandToAllPuzzles("reset"));
	}

	private Future finishPuzzles() {
		return EXECUTOR.submit(() -> sendCommandToAllPuzzles("finish"));
	}

	private Future sendCommandToPuzzle(String puzzleName, String name) {
		return EXECUTOR.submit(() ->puzzleManager.getPuzzle(puzzleName)
				.ifPresent(puzzle -> puzzle.getPC_PuzzleCommand(name)
								.ifPresent(s -> writeData(puzzle.getIdentifier(), s))
				)
		);
	}

	private void sendCommandToAllPuzzles(String name) {
		puzzleManager.getPuzzles().values()
				.forEach(puzzle -> puzzle.getPC_PuzzleCommand(name)
                        .ifPresent(s -> writeData(puzzle.getIdentifier(), s))
				);
	}

	private Future checkInitPuzzles() {
		return EXECUTOR.submit(() -> {
			Boolean init = false;
			while(!init) {
				try {
					init = puzzleManager.getPuzzles().values().stream()
							.allMatch(puzzle -> puzzle.getPuzzleState().equals("stopped"));
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			stateMachine.sendEvent("INITCHECK");
		});
	}

}
