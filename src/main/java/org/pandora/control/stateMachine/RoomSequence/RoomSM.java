package org.pandora.control.stateMachine.RoomSequence;

import gnu.io.SerialPortEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.pandora.control.clock.CountDown;
import org.pandora.control.model.Puzzle;
import org.pandora.control.model.event.RoomEvent;
import org.pandora.control.model.state.PuzzleState;
import org.pandora.control.model.state.RoomState;
import org.pandora.control.music.AudioManager;
import org.pandora.control.puzzle.PuzzleManager;
import org.pandora.control.serialcomm.SerialCommunicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = false)
@Component
@Value
@Slf4j
public class RoomSM extends SerialCommunicator {

	private ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
	private StateMachine<RoomState, RoomEvent> stateMachine;
	private PuzzleManager puzzleManager;
	private AudioManager audioManager;
	private CountDown timeRemaining;
	
	@Autowired
	public RoomSM(CountDown timeRemaining, PuzzleManager puzzleManager, AudioManager audioManager) throws Exception {
		super();
		stateMachine = buildMachine();
		this.timeRemaining = timeRemaining;
		this.puzzleManager = puzzleManager;
		this.audioManager = audioManager;
	}

	public void resetSM() throws Exception {
		EXECUTOR = Executors.newFixedThreadPool(10);
		checkForSerialPorts();
		stateMachine = buildMachine();
	}

	public void startPuzzles() {
		stateMachine.sendEvent(RoomEvent.START);
	}

	public void startSM(String port) throws Exception {
		initializePort(port);
		stateMachine.start();
		initPuzzles();
		checkInitPuzzles().get(15, TimeUnit.MINUTES);
	}

	public void stopSM() {
		stateMachine.stop();
	}
	
	private StateMachine<RoomState, RoomEvent> buildMachine() throws Exception {
	    Builder<RoomState, RoomEvent> builder = StateMachineBuilder.builder();

	    builder.configureStates()
	        .withStates()
	        .initial(RoomState.INIT)
	        .end(RoomState.FINISHED)
	        .states(EnumSet.allOf(RoomState.class));

	    builder.configureTransitions()
		    .withExternal()
		    .source(RoomState.INIT).target(RoomState.IDLE).event(RoomEvent.INITCHECK).and()
	        .withExternal()
	        .source(RoomState.IDLE).target(RoomState.PUZZLES).event(RoomEvent.START).action(startRoom()).and()
	        .withExternal()
	        .source(RoomState.PUZZLES).target(RoomState.FINISHED).event(RoomEvent.FINISH).action(finalizeSM());

	    return builder.build();
	}

	private Action<RoomState, RoomEvent> finalizeSM() {
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
		};
	}

	private Action<RoomState, RoomEvent> startRoom() {
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
		return EXECUTOR.submit(() ->
				puzzleManager.getPuzzles().values()
					.forEach(puzzle ->
						writeData(puzzle.getIdentifier(), puzzle.getPC_Puzzle().get("reset").getName())
					));
	}

	private Future checkInitPuzzles() {
		return EXECUTOR.submit(() -> {
			Boolean init = false;
			while(!init) {
				try {
					init = puzzleManager.getPuzzles().values().stream()
							.allMatch(puzzle -> puzzle.getPuzzleState() == PuzzleState.STOPPED);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			stateMachine.sendEvent(RoomEvent.INITCHECK);
		});
	}

}
