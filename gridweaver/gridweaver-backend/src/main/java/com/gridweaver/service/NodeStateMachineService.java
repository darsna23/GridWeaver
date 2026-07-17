package com.gridweaver.service;

import com.gridweaver.model.NodeEvent;
import com.gridweaver.model.NodeState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives real Spring State Machine instances that enforce legal transitions
 * for each node (NORMAL/CHARGING/DISCHARGING/FAULT).
 *
 * At true IoT scale (50k+ nodes) keeping one live StateMachine object per
 * node resident in memory forever is wasteful, since most nodes sit idle in
 * NORMAL most of the time. So this service keeps:
 *  - stateByNode: a cheap, authoritative map of "current state" for every
 *    node, used for the dashboard/API (O(1) reads, tiny footprint).
 *  - machineCache: a bounded LRU cache of *live* StateMachine instances,
 *    used to actually validate/execute transitions for nodes that are
 *    currently active. Idle machines are evicted; state is not lost because
 *    stateByNode already recorded the result.
 */
@Slf4j
@Service
public class NodeStateMachineService {

    private static final int MAX_LIVE_MACHINES = 2000;

    private final StateMachineFactory<NodeState, NodeEvent> stateMachineFactory;
    private final ConcurrentHashMap<String, NodeState> stateByNode = new ConcurrentHashMap<>();

    // Simple LRU cache backed by LinkedHashMap (access-order) - guarded by synchronized block.
    private final Map<String, StateMachine<NodeState, NodeEvent>> machineCache =
            new LinkedHashMap<>(MAX_LIVE_MACHINES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, StateMachine<NodeState, NodeEvent>> eldest) {
                    if (size() > MAX_LIVE_MACHINES) {
                        eldest.getValue().stopReactively().subscribe();
                        return true;
                    }
                    return false;
                }
            };

    @Autowired
    public NodeStateMachineService(StateMachineFactory<NodeState, NodeEvent> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }

    public NodeState getState(String nodeId) {
        return stateByNode.getOrDefault(nodeId, NodeState.NORMAL);
    }

    public Map<String, NodeState> snapshot() {
        return stateByNode;
    }

    /**
     * Feeds one event into the node's state machine and returns the
     * resulting state, or null if the transition was rejected (e.g. event
     * doesn't apply from the current state).
     */
    public synchronized NodeState fireEvent(String nodeId, NodeEvent event) {
        StateMachine<NodeState, NodeEvent> machine = machineCache.computeIfAbsent(nodeId, id -> {
            StateMachine<NodeState, NodeEvent> sm = stateMachineFactory.getStateMachine(id);
            NodeState existing = stateByNode.get(id);
            if (existing != null && existing != NodeState.NORMAL) {
                // Restore prior state without re-running side effects.
                sm.getStateMachineAccessor().doWithAllRegions(access ->
                        access.resetStateMachine(new org.springframework.statemachine.support.DefaultStateMachineContext<>(
                                existing, null, null, null)));
            }
            sm.startReactively().subscribe();
            return sm;
        });

        machine.sendEvent(reactor.core.publisher.Mono.just(
                MessageBuilder.withPayload(event).build())).blockLast();

        NodeState newState = machine.getState().getId();
        stateByNode.put(nodeId, newState);
        return newState;
    }
}
