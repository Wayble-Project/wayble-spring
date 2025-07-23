package com.wayble.server.direction.service;

import com.wayble.server.direction.entity.DirectionType;
import com.wayble.server.direction.entity.Edge;
import com.wayble.server.direction.entity.Node;
import org.springframework.stereotype.Service;

@Service
public class EdgeService {
    public Edge createEdge(Node startNode, Node endNode, DirectionType edgeType) {

        Edge edge = Edge.builder()
                .edgeType(edgeType)
                .startNode(startNode)
                .endNode(endNode)
                .route(null)
                .build();

        return edge;
    }
}
