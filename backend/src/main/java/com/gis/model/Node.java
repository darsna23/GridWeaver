package com.gis.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nodeId;
    private String zone;
    private double latitude;
    private double longitude;
    
    @Enumerated(EnumType.STRING)
    private NodeState state;
    
    private double powerOutput; 
    private double batteryLevel;
    private LocalDateTime lastUpdate;
    private boolean fault;
}