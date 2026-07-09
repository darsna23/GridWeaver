// src/main/java/com/gis/DataInitializer.java
package com.gis;

import com.gis.service.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final NodeService nodeService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing database with sample nodes...");
        try {
            nodeService.initializeNodes(50231);
            log.info("✅ Database initialized with 50,231 nodes");
        } catch (Exception e) {
            log.error("Error initializing database: {}", e.getMessage());
        }
    }
}