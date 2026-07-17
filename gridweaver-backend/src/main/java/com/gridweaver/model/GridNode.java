package com.gridweaver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single physical/virtual grid node (smart meter, battery cabinet,
 * transformer, etc). Each GridNode owns one Spring State Machine instance
 * (keyed by nodeId) that governs its {@link NodeState}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GridNode {
    private String nodeId;          // e.g. NODE-AP19
    private double latitude;
    private double longitude;
    private PowerZone zone;
    private NodeState state;
    private double powerMw;         // instantaneous power reading
    private double batteryPercent;  // 0-100
    private double loadPercent;     // 0-100, grid load driving DISCHARGE
    private long lastUpdated;
}
