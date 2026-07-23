package com.gridweaver.dto;

import com.gridweaver.model.GridNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Everything the dashboard needs to render one frame: KPI cards, node
 * markers for the map, the state-count legend, and per-zone power totals.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GridOverviewDTO {
    private int totalNodes;
    private int onlineNodes;
    private double activePowerMw;
    private double gridStabilityPercent;
    private String systemStatus;           // "Stable" / "Degraded" / "Critical"

    private Map<String, Integer> nodeStateCounts;   // NORMAL, CHARGING, DISCHARGING, FAULT
    private Map<String, Double> powerByZone;        // ZONE_A..ZONE_E

    private List<GridNode> nodes;          // markers to draw on the map
    private List<EventLogDTO> recentEvents;
}
