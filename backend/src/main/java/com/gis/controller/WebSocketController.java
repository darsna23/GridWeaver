package com.gis.controller;

import com.gis.model.Node;
import com.gis.service.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    private final NodeService nodeService;
    
    @MessageMapping("/nodes/all")
    @SendTo("/topic/nodes")
    public Iterable<Node> getAllNodes() {
        return nodeService.getAllNodes();
    }
}