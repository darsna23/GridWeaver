package com.gridweaver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventLogDTO {
    private long timestamp;
    private String nodeId;
    private String label;   // e.g. "NORMAL->DISCHARGING" or "FAULT_DETECTED"
    private String severity; // "info" | "warn" | "critical" | "success"
}
