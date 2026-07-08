package com.gis.service;

import com.gis.model.Node;
import com.gis.model.NodeState;
import com.gis.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeService {
    
    private final NodeRepository nodeRepository;
    private final Random random = new Random();
    
    @Transactional
    public List<Node> initializeNodes(int count) {
        log.info("Initializing {} nodes", count);
        List<Node> nodes = new java.util.ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Node node = new Node();
            node.setNodeId("NODE-" + UUID.randomUUID().toString().substring(0, 8));
            node.setZone("Zone-" + random.nextInt(10));
            node.setLatitude(40.7128 + (random.nextDouble() - 0.5) * 0.1);
            node.setLongitude(-74.0060 + (random.nextDouble() - 0.5) * 0.1);
            node.setState(NodeState.NORMAL);
            node.setPowerOutput(random.nextDouble() * 50);
            node.setBatteryLevel(50 + random.nextDouble() * 40);
            node.setLastUpdate(LocalDateTime.now());
            node.setFault(false);
            nodes.add(node);
        }
        
        return nodeRepository.saveAll(nodes);
    }
    
    @Transactional
    public Node updateNodeState(String nodeId, NodeState newState) {
        Node node = nodeRepository.findByNodeId(nodeId)
            .orElseThrow(() -> new RuntimeException("Node not found: " + nodeId));
        
        node.setState(newState);
        node.setLastUpdate(LocalDateTime.now());
        if (newState == NodeState.FAULT) {
            node.setFault(true);
        }
        
        log.info("Node {} state updated to {}", nodeId, newState);
        return nodeRepository.save(node);
    }
    
    public Node updateNode(String nodeId, Node updateData) {
        Node node = nodeRepository.findByNodeId(nodeId)
            .orElseThrow(() -> new RuntimeException("Node not found: " + nodeId));
        
        if (updateData.getPowerOutput() != 0) {
            node.setPowerOutput(updateData.getPowerOutput());
        }
        if (updateData.getBatteryLevel() != 0) {
            node.setBatteryLevel(updateData.getBatteryLevel());
        }
        node.setLastUpdate(LocalDateTime.now());
        
        return nodeRepository.save(node);
    }
    
    public List<Node> getAllNodes() {
        return nodeRepository.findAll();
    }
    
    public Node getNode(String nodeId) {
        return nodeRepository.findByNodeId(nodeId)
            .orElseThrow(() -> new RuntimeException("Node not found: " + nodeId));
    }
    
    public long countByState(NodeState state) {
        return nodeRepository.countByState(state);
    }
    
    public double getTotalPowerOutput() {
        return nodeRepository.sumPowerOutput();
    }
}