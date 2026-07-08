package com.gis.service;

import com.gis.model.Node;
import com.gis.model.NodeEvent;
import com.gis.model.NodeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateMachineService {
    
    private final NodeService nodeService;
    private final StateMachineService<NodeState, NodeEvent> stateMachineService;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private final Map<String, StateMachine<NodeState, NodeEvent>> nodeStateMachines = new ConcurrentHashMap<>();
    
    public void processGridLoad(double loadPercentage) {
        if (loadPercentage > 80) {
            log.info("Grid load high ({}%), initiating charging for nodes", loadPercentage);
            nodeService.getAllNodes().stream()
                .filter(n -> n.getState() == NodeState.NORMAL)
                .limit(1000)
                .forEach(node -> sendEvent(node.getNodeId(), NodeEvent.GRID_LOAD_HIGH));
        } else if (loadPercentage < 30) {
            log.info("Grid load low ({}%), initiating discharging for nodes", loadPercentage);
            nodeService.getAllNodes().stream()
                .filter(n -> n.getState() == NodeState.NORMAL)
                .limit(1000)
                .forEach(node -> sendEvent(node.getNodeId(), NodeEvent.GRID_LOAD_LOW));
        }
    }
    
    public void sendEvent(String nodeId, NodeEvent event) {
        try {
            StateMachine<NodeState, NodeEvent> sm = getOrCreateStateMachine(nodeId);
            sm.sendEvent(event);
            
            // Send update via WebSocket
            Node node = nodeService.getNode(nodeId);
            messagingTemplate.convertAndSend("/topic/nodes/" + nodeId, node);
            
            // Send to Kafka for decoupling
            kafkaTemplate.send("node-events", nodeId, 
                Map.of("event", event, "node", node));
            
            log.info("Event {} sent to node {}", event, nodeId);
        } catch (Exception e) {
            log.error("Error sending event to node {}: {}", nodeId, e.getMessage());
        }
    }
    
    private StateMachine<NodeState, NodeEvent> getOrCreateStateMachine(String nodeId) {
        return nodeStateMachines.computeIfAbsent(nodeId, id -> {
            StateMachine<NodeState, NodeEvent> sm = stateMachineService.acquireStateMachine(nodeId);
            sm.start();
            return sm;
        });
    }
    
    public void handleKafkaEvent(String nodeId, NodeEvent event) {
        sendEvent(nodeId, event);
    }
}