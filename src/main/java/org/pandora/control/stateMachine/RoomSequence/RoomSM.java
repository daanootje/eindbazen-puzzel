package org.pandora.control.stateMachine.RoomSequence;

import java.util.EnumSet;

import org.pandora.control.clock.CountDown;
import org.pandora.control.model.event.RoomEvent;
import org.pandora.control.model.state.RoomState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.stereotype.Component;

import lombok.Value;

@Component
@Value
public class RoomSM {

	private StateMachine<RoomState, RoomEvent> stateMachine;
	private CountDown timeRemaining;
	
	@Autowired
	public RoomSM(CountDown timeRemaining) throws Exception {
		stateMachine = buildMachine();
		this.timeRemaining = timeRemaining;
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
		
}
