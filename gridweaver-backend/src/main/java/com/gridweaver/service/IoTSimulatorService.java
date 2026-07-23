package com.gridweaver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridweaver.config.KafkaConfig;
import com.gridweaver.model.GridNode;
import com.gridweaver.model.NodeState;
import com.gridweaver.model.PowerZone;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates a fleet of IoT power-grid nodes (smart meters / battery
 * cabinets / transformers), each running its own "connection" loop on a
 * dedicated virtual thread. At NODE_COUNT = 10,000+ this would exhaust
 * platform threads quickly; with Executors.newVirtualThreadPerTaskExecutor()
 * (Project Loom) the JVM carries all of them cheaply.
 *
 * Each simulated node publishes a telemetry reading to Kafka every tick,
 * which decouples "raw ingestion rate" from "state machine processing
 * rate" downstream (see KafkaTelemetryConsumer).
 */
@Slf4j
@Service
public class IoTSimulatorService {

    @Value("${gridweaver.simulation.node-count:10000}")
    private int nodeCount;

    @Value("${gridweaver.simulation.tick-ms:2000}")
    private long tickMs;

    private final ExecutorService virtualThreadExecutor;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<GridNode> nodes = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    // Roughly centered over a generic metro area; spread nodes around it.
    private static final double CENTER_LAT = 28.6139;
    private static final double CENTER_LNG = 77.2090;

    public IoTSimulatorService(
            @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutor,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < nodeCount; i++) {
            nodes.add(buildRandomNode(i));
        }
        log.info("IoTSimulatorService initialized {} nodes", nodes.size());
        start();
    }

    public List<GridNode> getNodes() {
        return nodes;
    }

    private GridNode buildRandomNode(int index) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double lat = CENTER_LAT + (rnd.nextDouble() - 0.5) * 4.0;
        double lng = CENTER_LNG + (rnd.nextDouble() - 0.5) * 4.0;
        PowerZone zone = PowerZone.values()[index % PowerZone.values().length];
        String id = "NODE-" + randomCode(rnd);
        return new GridNode(id, lat, lng, zone, NodeState.NORMAL,
                800 + rnd.nextDouble() * 400, // powerMw baseline like the reference UI
                40 + rnd.nextDouble() * 60,   // batteryPercent
                30 + rnd.nextDouble() * 50,   // loadPercent
                System.currentTimeMillis());
    }

    private String randomCode(ThreadLocalRandom rnd) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    /**
     * Spins up one virtual thread PER NODE. Each thread simulates a
     * persistent IoT socket: it wakes up every tickMs, perturbs its
     * reading, and publishes a JSON telemetry message to Kafka.
     * This is the "10,000 concurrent socket connections handled via
     * Virtual Threads" requirement from the dev plan.
     */
    public void start() {
        if (!running.compareAndSet(false, true)) return;
        for (GridNode node : nodes) {
            virtualThreadExecutor.submit(() -> runNodeLoop(node));
        }
    }

    private void runNodeLoop(GridNode node) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        while (running.get()) {
            try {
                perturb(node, rnd);
                String json = objectMapper.writeValueAsString(node);
                kafkaTemplate.send(KafkaConfig.TELEMETRY_TOPIC, node.getNodeId(), json);
                Thread.sleep(tickMs + rnd.nextInt(500));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Simulator error for {}: {}", node.getNodeId(), e.getMessage());
            }
        }
    }

    private void perturb(GridNode node, ThreadLocalRandom rnd) {
        node.setPowerMw(Math.max(0, node.getPowerMw() + (rnd.nextDouble() - 0.5) * 20));
        node.setLoadPercent(clamp(node.getLoadPercent() + (rnd.nextDouble() - 0.5) * 8, 0, 100));
        node.setBatteryPercent(clamp(node.getBatteryPercent() + (rnd.nextDouble() - 0.5) * 6, 0, 100));
        // Rare random fault injection so the FAULT state / event log stays populated.
        node.setLastUpdated(System.currentTimeMillis());
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
