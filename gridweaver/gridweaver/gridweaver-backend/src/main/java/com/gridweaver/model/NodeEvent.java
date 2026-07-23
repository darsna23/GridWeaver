package com.gridweaver.model;

/**
 * Events fed into a node's state machine to trigger transitions.
 * These are derived from incoming telemetry (grid load, battery level,
 * fault flags) by {@code NodeStateMachineService}.
 */
public enum NodeEvent {
    LOAD_HIGH,       // grid load > 80% -> begin DISCHARGE
    LOAD_NORMAL,     // load back to a healthy range -> NORMAL
    BATTERY_LOW,     // battery under threshold -> begin CHARGING
    BATTERY_FULL,    // battery charged -> NORMAL
    FAULT_DETECTED,  // hardware/comm fault -> FAULT
    FAULT_CLEARED    // fault resolved -> NORMAL
}
