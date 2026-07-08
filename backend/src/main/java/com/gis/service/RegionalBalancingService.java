package com.gis.service;

import com.gis.model.Node;
import com.gis.model.NodeEvent;
import com.gis.model.NodeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionalBalancingService {
    
    private final NodeService nodeService;
    private final StateMachineService stateMachineService;
    
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void balanceRegions() {
        log.info("Starting regional balancing");
        
        Map<String, List<Node>> nodesByZone = nodeService.getAllNodes().stream()
            .collect(Collectors.groupingBy(Node::getZone));
        
        for (Map.Entry<String, List<Node>> entry : nodesByZone.entrySet()) {
            String zone = entry.getKey();
            List<Node> nodes = entry.getValue();
            
            double zonePower = nodes.stream()
                .filter(n -> n.getState() == NodeState.DISCHARGING)
                .mapToDouble(Node::getPowerOutput)
                .sum();
            
            double zoneDemand = nodes.stream()
                .filter(n -> n.getState() == NodeState.CHARGING)
                .count() * 20; // Average demand per charging node
            
            if (zonePower < zoneDemand * 0.8) {
                log.info("Zone {} underpowered, initiating power transfer", zone);
                // Find zone with surplus
                nodesByZone.entrySet().stream()
                    .filter(e -> !e.getKey().equals(zone))
                    .forEach(e -> {
                        double surplus = e.getValue().stream()
                            .filter(n -> n.getState() == NodeState.CHARGING)
                            .count() * 20 - 
                            e.getValue().stream()
                            .filter(n -> n.getState() == NodeState.DISCHARGING)
                            .mapToDouble(Node::getPowerOutput)
                            .sum();
                        
                        if (surplus > 0) {
                            log.info("Transferring {} MW from Zone {} to Zone {}", 
                                surplus, e.getKey(), zone);
                            // Trigger power transfer events
                            e.getValue().stream()
                                .filter(n -> n.getState() == NodeState.CHARGING)
                                .limit((long) (surplus / 20))
                                .forEach(n -> {
                                    stateMachineService.sendEvent(n.getNodeId(), 
                                        NodeEvent.POWER_TRANSFER_REQUEST);
                                });
                        }
                    });
            }
        }
    }
}