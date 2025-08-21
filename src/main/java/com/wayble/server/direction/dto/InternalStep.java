package com.wayble.server.direction.dto;

import java.util.List;

import com.wayble.server.direction.dto.response.TransportationResponseDto;
import com.wayble.server.direction.entity.transportation.Node;
import com.wayble.server.direction.entity.type.DirectionType;

// 대중교통 길찾기에 사용하기 위한 내부용 DTO
public record InternalStep(
    DirectionType mode,
    List<TransportationResponseDto.MoveInfo> moveInfo,
    String routeName,
    Integer moveNumber,
    TransportationResponseDto.BusInfo busInfo,
    TransportationResponseDto.SubwayInfo subwayInfo,
    String from,
    String to,
    Long routeId,
    Node startNode,
    Node endNode
) {}