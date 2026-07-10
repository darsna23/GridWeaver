package com.gridweaver.controller;

import com.gridweaver.dto.GridOverviewDTO;
import com.gridweaver.service.GridNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grid")
@CrossOrigin(origins = "*") // dev only - restrict in production
public class GridController {

    private final GridNodeService gridNodeService;

    @Autowired
    public GridController(GridNodeService gridNodeService) {
        this.gridNodeService = gridNodeService;
    }

    /** Used by the React app on first load, before the WebSocket takes over. */
    @GetMapping("/overview")
    public GridOverviewDTO getOverview() {
        return gridNodeService.buildOverview();
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
