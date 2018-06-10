package org.pandora.control.stateMachine;

import gnu.io.SerialPortEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.pandora.control.clock.CountDown;
import org.pandora.control.music.AudioManager;
import org.pandora.control.puzzle.Puzzle;
import org.pandora.control.puzzle.PuzzleManager;
import org.pandora.control.serialcomm.SerialCommunicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableStateMachineFactory
public class RoomSM extends SerialCommunicator {

	private ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
	private StateMachine<String, String> stateMachine;
	private LinkedList<State> puzzleSequence;
	private PuzzleManager puzzleManager;
	private AudioManager audioManager;
	private CountDown timeRemaining;

	private Boolean started = false;
	private Boolean reset = false;
	
	@Autowired
	public RoomSM(CountDown timeRemaining, PuzzleManager puzzleManager, AudioManager audioManager) throws Exception {
		super();
		stateMachine = buildMachine();
		this.timeRemaining = timeRemaining;
		this.puzzleManager = puzzleManager;
		this.audioManager = audioManager;
	}

	public void resetSM() {
		try {
			stopSM();
			EXECUTOR = Executors.newFixedThreadPool(10);
			checkForSerialPorts();
			stateMachine = buildMachine();
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
			try {
				initializePort(port);
				stateMachine.start();
				initPuzzles();
				checkInitPuzzles().get(15, TimeUnit.MINUTES);
				started = true;
				reset = false;
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				log.error(String.format("Something went wrong starting statemachine - %s", e.getMessage()));
			}
		}
	}

	public void stopSM() {
		stateMachine.stop();
		started = false;
	}

	public void finishSM() {
		finishPuzzles();
		stateMachine.stop();
		finalizeSM();
	}

	public String getState() {
		return stateMachine.getState().getId();
	}
	
	private StateMachine<String, String> buildMachine() throws Exception {
	    Builder<String, String> builder = StateMachineBuilder.builder();

	    builder.configureStates()
	        .withStates()
	        .initial("INIT")
	        .end("FINISHED")
	        .states(puzzleSequence.stream().map(State::getCurrent).collect(Collectors.toSet()));

	    builder.configureTransitions()
		    .withExternal()
		    .source("INIT").target("IDLE").event("INITCHECK").and()
	        .withExternal()
	        .source("IDLE").target("PUZZLES").event("START").action(startRoom()).and()
	        .withExternal()
	        .source("PUZZLES").target("FINISHED").event("FINISH").action(finalizeSM());

	    return builder.build();
	}

	private Action<String, String> finalizeSM() {
		return ctx -> {
			log.info("Finalizing room");
			EXECUTOR.shutdownNow();
			log.info("Stop all audio");
			audioManager.intializeAllMusic();
			log.info("Disconnecting serial port");
			disconnect();
			log.info("Stop timer");
			timeRemaining.pause();
			log.info("Storing essential data");
			storeData();
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

	private void storeData() {
		//TODO
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

	@Data
	private class State {
		private String current;
		private String to;
		private String event_trigger;
		private String action;
	}

}
