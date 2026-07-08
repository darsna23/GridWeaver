package com.gis.config;

import com.gis.model.NodeState;
import com.gis.model.NodeEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends StateMachineConfigurerAdapter<NodeState, NodeEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<NodeState, NodeEvent> states) throws Exception {
        states
            .withStates()
            .initial(NodeState.NORMAL)
            .states(EnumSet.allOf(NodeState.class))
            .stateEntry(NodeState.CHARGING, context -> {
                System.out.println("Entering CHARGING state for node: " + context.getStateMachine().getId());
            })
            .stateExit(NodeState.CHARGING, context -> {
                System.out.println("Exiting CHARGING state for node: " + context.getStateMachine().getId());
            });
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<NodeState, NodeEvent> transitions) throws Exception {
        transitions
            // Normal -> Charging (grid load high)
            .withExternal()
                .source(NodeState.NORMAL).target(NodeState.CHARGING)
                .event(NodeEvent.GRID_LOAD_HIGH)
            .and()
            // Normal -> Discharging (grid load low)
            .withExternal()
                .source(NodeState.NORMAL).target(NodeState.DISCHARGING)
                .event(NodeEvent.GRID_LOAD_LOW)
            .and()
            // Charging -> Normal (battery full)
            .withExternal()
                .source(NodeState.CHARGING).target(NodeState.NORMAL)
                .event(NodeEvent.BATTERY_FULL)
            .and()
            // Discharging -> Normal (battery low)
            .withExternal()
                .source(NodeState.DISCHARGING).target(NodeState.NORMAL)
                .event(NodeEvent.BATTERY_LOW)
            .and()
            // Any -> Fault
            .withExternal()
                .source(NodeState.NORMAL).target(NodeState.FAULT)
                .event(NodeEvent.FAULT_DETECTED)
            .and()
            .withExternal()
                .source(NodeState.CHARGING).target(NodeState.FAULT)
                .event(NodeEvent.FAULT_DETECTED)
            .and()
            .withExternal()
                .source(NodeState.DISCHARGING).target(NodeState.FAULT)
                .event(NodeEvent.FAULT_DETECTED)
            .and()
            // Fault -> Normal
            .withExternal()
                .source(NodeState.FAULT).target(NodeState.NORMAL)
                .event(NodeEvent.FAULT_CLEARED);
    }
}