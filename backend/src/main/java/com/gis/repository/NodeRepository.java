// src/main/java/com/gis/repository/NodeRepository.java
package com.gis.repository;

import com.gis.model.Node;
import com.gis.model.NodeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {
    Optional<Node> findByNodeId(String nodeId);
    
    long countByState(NodeState state);
    
    @Query("SELECT COUNT(n) FROM Node n WHERE n.state != 'FAULT'")
    long countOnlineNodes();
    
    @Query("SELECT SUM(n.powerOutput) FROM Node n WHERE n.state = 'DISCHARGING'")
    Double sumPowerOutput();
    
    List<Node> findByZone(String zone);
    
    List<Node> findByState(NodeState state);
    
    @Query("SELECT COUNT(n) FROM Node n WHERE n.state = :state AND n.fault = false")
    long countByStateAndNotFault(NodeState state);
}