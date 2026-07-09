// src/main/java/com/gis/model/DashboardStats.java
package com.gis.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalNodes;
    private Long onlineNodes;
    private Long normalNodes;
    private Long chargingNodes;
    private Long dischargingNodes;
    private Long faultNodes;
    private Double activePowerMW;
    private Double gridStability;
    private String status;
}