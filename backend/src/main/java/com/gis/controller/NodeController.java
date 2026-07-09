// src/main/java/com/gis/controller/NodeController.java
package com.gis.controller;

import com.gis.model.DashboardStats;
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
@CrossOrigin(origins = "http://localhost:3000")
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
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(nodeService.getStats());
    }
    
    @PostMapping("/initialize")
    public ResponseEntity<List<Node>> initializeNodes(@RequestParam(defaultValue = "1000") int count) {
        return ResponseEntity.ok(nodeService.initializeNodes(count));
    }
    
    @PutMapping("/{nodeId}/state")
    public ResponseEntity<Node> updateState(
            @PathVariable String nodeId,
            @RequestParam NodeState state) {
        return ResponseEntity.ok(nodeService.updateNodeState(nodeId, state));
    }
    
    @PutMapping("/{nodeId}")
    public ResponseEntity<Node> updateNode(@PathVariable String nodeId, @RequestBody Node node) {
        node.setNodeId(nodeId);
        return ResponseEntity.ok(nodeService.updateNode(node));
    }
}