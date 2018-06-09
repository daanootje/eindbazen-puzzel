package org.pandora.control.stateMachine.RoomSequence;

import gnu.io.SerialPortEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.pandora.control.clock.CountDown;
import org.pandora.control.model.event.RoomEvent;
import org.pandora.control.model.state.RoomState;
import org.pandora.control.puzzle.PuzzleManager;
import org.pandora.control.serialcomm.SerialCommunicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@EqualsAndHashCode(callSuper = false)
@Component
@Value
@Slf4j
public class RoomSM extends SerialCommunicator {

	private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
	private StateMachine<RoomState, RoomEvent> stateMachine;
	private PuzzleManager puzzleManager;
	private CountDown timeRemaining;
	
	@Autowired
	public RoomSM(CountDown timeRemaining, PuzzleManager puzzleManager) throws Exception {
		super();
		stateMachine = buildMachine();
		this.timeRemaining = timeRemaining;
		this.puzzleManager = puzzleManager;
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
	        .source(RoomState.IDLE).target(RoomState.PUZZLES).event(RoomEvent.START).and()
	        .withExternal()
	        .source(RoomState.PUZZLES).target(RoomState.FINISHED).event(RoomEvent.FINISH);

	    return builder.build();
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

	private Future initPuzzles() {
		return EXECUTOR.submit(() -> {
			puzzleManager.getPuzzles().values().stream()
					.forEach(writeData());
		});
	}

	private void applyPuzzle_PC(byte identifier, String msg) {
		puzzleManager.getPuzzle(identifier)
				.map(puzzle -> {
					puzzle.apply();
				});
	}

	public void applyPC_Puzzle(byte identifier, String msg) {
		puzzleManager.getPuzzle(identifier)
				.map(puzzle -> {

				});
	}
}
