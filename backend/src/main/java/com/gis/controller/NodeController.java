package com.gis.controller;

import com.gis.model.Node;
import com.gis.model.NodeState;
import com.gis.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeController {
    
    private final NodeService nodeService;
    
    @GetMapping
    public ResponseEntity<List<Node>> getAllNodes() {
        return ResponseEntity.ok(nodeService.getAllNodes());
    }
    
    @GetMapping("/{nodeId}")
    public ResponseEntity<Node> getNode(@PathVariable String nodeId) {
        return ResponseEntity.ok(nodeService.getNode(nodeId));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNodes", nodeService.getAllNodes().size());
        stats.put("onlineNodes", nodeService.getAllNodes().stream()
            .filter(n -> n.getState() != NodeState.FAULT).count());
        stats.put("normalNodes", nodeService.countByState(NodeState.NORMAL));
        stats.put("chargingNodes", nodeService.countByState(NodeState.CHARGING));
        stats.put("dischargingNodes", nodeService.countByState(NodeState.DISCHARGING));
        stats.put("faultNodes", nodeService.countByState(NodeState.FAULT));
        stats.put("activePowerMW", nodeService.getTotalPowerOutput());
        return ResponseEntity.ok(stats);
    }
}