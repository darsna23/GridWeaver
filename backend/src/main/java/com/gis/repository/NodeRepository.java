package com.gis.repository;

import com.gis.model.Node;
import com.gis.model.NodeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findByNodeId(String nodeId);

    long countByState(NodeState state);

    @Query("SELECT COALESCE(SUM(n.powerOutput), 0) FROM Node n")
    double sumPowerOutput();
}