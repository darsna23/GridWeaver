package com.gridweaver.service;

import com.gridweaver.dto.EventLogDTO;
import com.gridweaver.dto.GridOverviewDTO;
import com.gridweaver.model.GridNode;
import com.gridweaver.model.NodeEvent;
import com.gridweaver.model.NodeState;
import com.gridweaver.model.PowerZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The authoritative "current world state" of the grid: one GridNode per
 * device, refreshed continuously by KafkaTelemetryConsumer, plus a rolling
 * event-log audit trail of every state transition (for the "Event Log /
 * Live Audit" panel in the UI).
 */
@Slf4j
@Service
public class GridNodeService {

    private static final int EVENT_LOG_CAPACITY = 60;
    // Only a sample of nodes is chosen to actually run through the
    // real Spring State Machine each tick (see NodeStateMachineService
    // for why); the rest keep their last known state until they're sampled.
    private static final double FAULT_PROBABILITY = 0.004;

    private final Map<String, GridNode> nodesById = new ConcurrentHashMap<>();
    private final Deque<EventLogDTO> eventLog = new ConcurrentLinkedDeque<>();
    private final NodeStateMachineService stateMachineService;

    @Autowired
    public GridNodeService(NodeStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    public void applyTelemetry(GridNode incoming) {
        NodeState previousState = stateMachineService.getState(incoming.getNodeId());
        incoming.setState(previousState);
        nodesById.put(incoming.getNodeId(), incoming);

        NodeEvent derivedEvent = deriveEvent(incoming, previousState);
        if (derivedEvent != null) {
            NodeState newState = stateMachineService.fireEvent(incoming.getNodeId(), derivedEvent);
            incoming.setState(newState);
            if (newState != previousState) {
                logTransition(incoming.getNodeId(), previousState, newState);
            }
        }
    }

    /**
     * Translates raw telemetry thresholds into a state-machine event,
     * mirroring the dev-plan rule: "if grid load > 80%, transition battery
     * to DISCHARGE", plus battery-based CHARGING and randomized FAULT
     * injection so the dashboard stays lively.
     */
    private NodeEvent deriveEvent(GridNode node, NodeState currentState) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        if (currentState == NodeState.FAULT) {
            return rnd.nextDouble() < 0.15 ? NodeEvent.FAULT_CLEARED : null;
        }
        if (rnd.nextDouble() < FAULT_PROBABILITY) {
            return NodeEvent.FAULT_DETECTED;
        }
        if (currentState == NodeState.DISCHARGING) {
            return node.getLoadPercent() <= 80 ? NodeEvent.LOAD_NORMAL : null;
        }
        if (currentState == NodeState.CHARGING) {
            return node.getBatteryPercent() >= 95 ? NodeEvent.BATTERY_FULL : null;
        }
        // currentState == NORMAL
        if (node.getLoadPercent() > 80) {
            return NodeEvent.LOAD_HIGH;
        }
        if (node.getBatteryPercent() < 25) {
            return NodeEvent.BATTERY_LOW;
        }
        return null;
    }

    private void logTransition(String nodeId, NodeState from, NodeState to) {
        String label = from + "->" + to;
        String severity = switch (to) {
            case FAULT -> "critical";
            case NORMAL -> from == NodeState.FAULT ? "success" : "info";
            default -> "info";
        };
        eventLog.addFirst(new EventLogDTO(System.currentTimeMillis(), nodeId, label, severity));
        while (eventLog.size() > EVENT_LOG_CAPACITY) {
            eventLog.removeLast();
        }
    }

    /** Builds the full snapshot the dashboard/WebSocket consumes. */
    public GridOverviewDTO buildOverview() {
        Collection<GridNode> all = nodesById.values();
        int total = all.size();

        Map<String, Integer> stateCounts = new LinkedHashMap<>();
        for (NodeState s : NodeState.values()) stateCounts.put(s.name(), 0);

        Map<String, Double> zonePower = new LinkedHashMap<>();
        for (PowerZone z : PowerZone.values()) zonePower.put(z.name(), 0.0);

        AtomicInteger online = new AtomicInteger();
        double[] totalPower = {0.0};

        for (GridNode n : all) {
            stateCounts.merge(n.getState().name(), 1, Integer::sum);
            zonePower.merge(n.getZone().name(), n.getPowerMw(), Double::sum);
            totalPower[0] += n.getPowerMw();
            if (n.getState() != NodeState.FAULT) online.incrementAndGet();
        }

        int faultCount = stateCounts.getOrDefault(NodeState.FAULT.name(), 0);
        double stability = total == 0 ? 100.0 : 100.0 * (1.0 - (double) faultCount / total);

        // Sample a bounded number of nodes for map markers so the payload stays reasonable.
        List<GridNode> markers = all.stream().limit(300).toList();

        return new GridOverviewDTO(
                total,
                online.get(),
                Math.round(totalPower[0] * 10.0) / 10.0,
                Math.round(stability * 10.0) / 10.0,
                stability > 95 ? "Stable" : stability > 85 ? "Degraded" : "Critical",
                stateCounts,
                zonePower,
                markers,
                new ArrayList<>(eventLog)
        );
    }
}
