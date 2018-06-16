package org.pandora.control.stateMachine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

@Configuration
@EnableStateMachine
public class Factory extends StateMachineConfigurerAdapter<String, String> {

    @Value(value = "classpath:statemachineTest.uml")
    private Resource location;

    @Override
    public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
        model
            .withModel()
            .factory(modelFactory());
    }

    private StateMachineModelFactory<String, String> modelFactory() {
        return new UmlStateMachineModelFactory(location);
    }

}
