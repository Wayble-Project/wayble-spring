package com.wayble.server.direction.dto;

import com.wayble.server.direction.entity.transportation.Edge;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public record TransportationGraphDto( // 노드별 연결 정보와 엣지별 가중치를 함께 관리하기 위한 dto
    Map<Long, List<Edge>> graph, // 노드별 연결 정보
    Map<Pair<Long, Long>, Integer> weightMap // 엣지별 가중치
) {}
