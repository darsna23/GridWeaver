package com.gridweaver.config;

import com.gridweaver.model.NodeEvent;
import com.gridweaver.model.NodeState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;


@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<NodeState, NodeEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<NodeState, NodeEvent> states) throws Exception {
        states
            .withStates()
            .initial(NodeState.NORMAL)
            .state(NodeState.CHARGING)
            .state(NodeState.DISCHARGING)
            .state(NodeState.FAULT);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<NodeState, NodeEvent> transitions) throws Exception {
        transitions
            // Normal <-> Charging
            .withExternal().source(NodeState.NORMAL).target(NodeState.CHARGING).event(NodeEvent.BATTERY_LOW).and()
            .withExternal().source(NodeState.CHARGING).target(NodeState.NORMAL).event(NodeEvent.BATTERY_FULL).and()

            // Normal <-> Discharging (grid load > 80%)
            .withExternal().source(NodeState.NORMAL).target(NodeState.DISCHARGING).event(NodeEvent.LOAD_HIGH).and()
            .withExternal().source(NodeState.DISCHARGING).target(NodeState.NORMAL).event(NodeEvent.LOAD_NORMAL).and()

            // Any operational state -> Fault
            .withExternal().source(NodeState.NORMAL).target(NodeState.FAULT).event(NodeEvent.FAULT_DETECTED).and()
            .withExternal().source(NodeState.CHARGING).target(NodeState.FAULT).event(NodeEvent.FAULT_DETECTED).and()
            .withExternal().source(NodeState.DISCHARGING).target(NodeState.FAULT).event(NodeEvent.FAULT_DETECTED).and()

            // Fault -> Normal once cleared
            .withExternal().source(NodeState.FAULT).target(NodeState.NORMAL).event(NodeEvent.FAULT_CLEARED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<NodeState, NodeEvent> config) throws Exception {
        config
            .withConfiguration()
            .autoStartup(true);
    }
}
