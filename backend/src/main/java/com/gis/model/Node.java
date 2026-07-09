// src/main/java/com/gis/model/Node.java
package com.gis.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nodes")
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nodeId;
    
    private String zone;
    private Double latitude;
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    private NodeState state;
    
    private Double powerOutput;
    private Double batteryLevel;
    private LocalDateTime lastUpdate;
    private Boolean fault;
}