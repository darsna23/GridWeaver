package com.gridweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridweaver.dto.GridOverviewDTO;
import com.gridweaver.websocket.GridWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GridBroadcastScheduler {

    private final GridNodeService gridNodeService;
    private final GridWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public GridBroadcastScheduler(GridNodeService gridNodeService, GridWebSocketHandler webSocketHandler) {
        this.gridNodeService = gridNodeService;
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastOverview() {
        try {
            GridOverviewDTO overview = gridNodeService.buildOverview();
            webSocketHandler.broadcast(objectMapper.writeValueAsString(overview));
        } catch (Exception e) {
            log.warn("Broadcast failed: {}", e.getMessage());
        }
    }
}
