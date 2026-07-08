package com.gis.service;

import com.gis.model.Node;
import com.gis.model.NodeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IotSimulator {
    
    private final NodeService nodeService;
    private final Random random = new Random();
    private final ExecutorService virtualThreadExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    @Scheduled(fixedDelay = 1000)
    public void simulateIoTData() {
        List<Node> nodes = nodeService.getAllNodes();
        
        // Simulate 10,000 concurrent connections
        for (int i = 0; i < Math.min(10000, nodes.size()); i++) {
            final int index = i;
            virtualThreadExecutor.submit(() -> {
                try {
                    Node node = nodes.get(index % nodes.size());
                    simulateNodeUpdate(node);
                } catch (Exception e) {
                    log.error("Error simulating node update: {}", e.getMessage());
                }
            });
        }
    }
    
    private void simulateNodeUpdate(Node node) {
        // Update power output
        double newPower = Math.max(0, node.getPowerOutput() + (random.nextDouble() - 0.5) * 10);
        node.setPowerOutput(newPower);
        
        // Update battery level
        double newBattery = Math.max(0, Math.min(100, 
            node.getBatteryLevel() + (random.nextDouble() - 0.5) * 2));
        node.setBatteryLevel(newBattery);
        
        // Random state changes
        if (random.nextDouble() < 0.01) { // 1% chance of state change
            NodeState[] states = NodeState.values();
            node.setState(states[random.nextInt(states.length)]);
        }
        
        // Random faults
        if (random.nextDouble() < 0.002) { // 0.2% chance of fault
            node.setState(NodeState.FAULT);
            node.setFault(true);
        }
        
        nodeService.updateNode(node.getNodeId(), node);
    }
}