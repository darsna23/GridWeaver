// src/main/java/com/gis/service/NodeService.java
package com.gis.service;

import com.gis.model.DashboardStats;
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
            node.setNodeId("NODE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
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
    
    public List<Node> getAllNodes() {
        return nodeRepository.findAll();
    }
    
    public Node getNode(String nodeId) {
        return nodeRepository.findByNodeId(nodeId)
            .orElseThrow(() -> new RuntimeException("Node not found: " + nodeId));
    }
    
    @Transactional
    public Node updateNodeState(String nodeId, NodeState newState) {
        Node node = getNode(nodeId);
        node.setState(newState);
        node.setLastUpdate(LocalDateTime.now());
        if (newState == NodeState.FAULT) {
            node.setFault(true);
        } else {
            node.setFault(false);
        }
        return nodeRepository.save(node);
    }
    
    @Transactional
    public Node updateNode(Node node) {
        Node existing = getNode(node.getNodeId());
        if (node.getPowerOutput() != null) {
            existing.setPowerOutput(node.getPowerOutput());
        }
        if (node.getBatteryLevel() != null) {
            existing.setBatteryLevel(node.getBatteryLevel());
        }
        if (node.getState() != null) {
            existing.setState(node.getState());
        }
        if (node.getFault() != null) {
            existing.setFault(node.getFault());
        }
        existing.setLastUpdate(LocalDateTime.now());
        return nodeRepository.save(existing);
    }
    
    public DashboardStats getStats() {
        long totalNodes = nodeRepository.count();
        long onlineNodes = nodeRepository.countOnlineNodes();
        long normalNodes = nodeRepository.countByState(NodeState.NORMAL);
        long chargingNodes = nodeRepository.countByState(NodeState.CHARGING);
        long dischargingNodes = nodeRepository.countByState(NodeState.DISCHARGING);
        long faultNodes = nodeRepository.countByState(NodeState.FAULT);
        Double totalPower = nodeRepository.sumPowerOutput();
        
        double activePowerMW = totalPower != null ? totalPower : 0;
        double stability = totalNodes > 0 ? ((double) onlineNodes / totalNodes) * 100 : 0;
        String status = stability > 95 ? "Stable" : stability > 85 ? "Degraded" : "Critical";
        
        return new DashboardStats(
            totalNodes,
            onlineNodes,
            normalNodes,
            chargingNodes,
            dischargingNodes,
            faultNodes,
            activePowerMW,
            stability,
            status
        );
    }
}