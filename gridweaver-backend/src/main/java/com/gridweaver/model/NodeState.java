package com.gridweaver.model;

/**
 * The states a grid node's Spring State Machine instance can be in.
 * Mirrors the "NODE STATES" legend on the dashboard: Normal, Charging,
 * Discharging, Fault.
 */
public enum NodeState {
    NORMAL,
    CHARGING,
    DISCHARGING,
    FAULT
}
