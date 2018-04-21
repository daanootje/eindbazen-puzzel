package org.pandora.control.stateMachine.RoomSequence;


import java.util.EnumSet;

import org.pandora.control.model.Event;
import org.pandora.control.model.state.SMState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachine
public class RoomSMConfig extends StateMachineConfigurerAdapter<SMState, Event> {

    @Override
    public void configure(StateMachineStateConfigurer<SMState, Event> states) throws Exception {
        states
            .withStates()
            .initial(SMState.INIT)
            .end(SMState.FINISHED)
            .states(EnumSet.allOf(SMState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<SMState, Event> transitions) throws Exception {
		SMState.PUZZLE1.
        transitions.withExternal()
                .source(SMState.INIT).target(SMState.IDLE).event(Event.INITCHECK).and()
                .withExternal()
                .source(SMState.IDLE).target(SMState.PUZZLESTATES.getPuzzle()..PUZZLE1).event(Event.START).and()
                .withExternal()
                .source(SMState.PUZZLE1).target(SMState.FINISHED).event(Event.);
    }

}
