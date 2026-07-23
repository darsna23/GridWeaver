package com.gridweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridweaver.config.KafkaConfig;
import com.gridweaver.model.GridNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumes raw telemetry from Kafka (published by IoTSimulatorService) and
 * hands each reading to GridNodeService, which runs it through the node's
 * state machine and updates the in-memory snapshot the dashboard reads.
 *
 * This is the layer that would scale out horizontally (more consumer
 * instances / partitions) independently of however many IoT devices are
 * publishing upstream.
 */
@Slf4j
@Service
public class KafkaTelemetryConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GridNodeService gridNodeService;

    @Autowired
    public KafkaTelemetryConsumer(GridNodeService gridNodeService) {
        this.gridNodeService = gridNodeService;
    }

    @KafkaListener(topics = KafkaConfig.TELEMETRY_TOPIC, groupId = "gridweaver-statemachine-group")
    public void onTelemetry(String payload) {
        try {
            GridNode incoming = objectMapper.readValue(payload, GridNode.class);
            gridNodeService.applyTelemetry(incoming);
        } catch (Exception e) {
            log.warn("Failed to process telemetry message: {}", e.getMessage());
        }
    }
}
