package com.wayble.server.direction.service;

import com.wayble.server.direction.dto.response.WayblePathResponse;
import com.wayble.server.direction.entity.Edge;
import com.wayble.server.direction.entity.Node;
import com.wayble.server.direction.entity.type.Type;
import com.wayble.server.direction.init.GraphInit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class WalkingServiceTest {

    @Mock
    private GraphInit graphInit;

    @Mock
    WaybleDijkstraService waybleDijkstraService;

    @InjectMocks
    private WalkingService walkingService;

    @BeforeEach
    void setUp() {
        Map<Long, Node> nodeMap = Map.of(
                1L, new Node(1L, 37.1, 127.1),
                2L, new Node(2L, 37.2, 127.1),
                3L, new Node(3L, 37.3, 127.1),
                4L, new Node(4L, 37.2, 127.2),
                5L, new Node(5L, 37.3, 127.2)
        );

        Map<Long, List<Edge>> adjacencyList = Map.of(
                1L, List.of(new Edge(1, 2, 100, List.of())),
                2L, List.of(new Edge(2, 3, 100, List.of()), new Edge(2, 4, 120, List.of())),
                3L, List.of(new Edge(3, 5, 90, List.of())),
                4L, List.of(new Edge(4, 5, 120, List.of()))
        );

        Map<Long, Type> markerMap = Map.of(
                2L, Type.RAMP,
                3L, Type.NONE,
                4L, Type.ELEVATOR
        );

        List<WayblePathResponse.WayblePoint> points = List.of(
                new WayblePathResponse.WayblePoint(37.1, 127.1, Type.NONE),
                new WayblePathResponse.WayblePoint(37.2, 127.1, Type.RAMP),
                new WayblePathResponse.WayblePoint(37.2, 127.2, Type.ELEVATOR),
                new WayblePathResponse.WayblePoint(37.3, 127.2, Type.NONE)
        );

        List<double[]> polyline = List.of(
                new double[]{37.1, 127.1},
                new double[]{37.2, 127.1},
                new double[]{37.2, 127.2},
                new double[]{37.3, 127.2}
        );

        when(graphInit.getNodeMap()).thenReturn(nodeMap);
        // when(graphInit.getMarkerMap()).thenReturn(markerMap);
        // when(graphInit.getGraph()).thenReturn(adjacencyList);
        when(waybleDijkstraService.createWayblePath(anyLong(), anyLong()))
                .thenReturn(WayblePathResponse.of(340, 300, points, polyline));
    }

    @Test
    @DisplayName("경사로가 있을 때의 웨이블 추천 경로")
    void rampWayblePathTest() {
        // given
        double startLat = 37.1;
        double startLon = 127.1;
        double endLat = 37.3;
        double endLon = 127.2;

        // when
        WayblePathResponse response = walkingService.findWayblePath(
                startLat, startLon, endLat, endLon
        );

        // then
        assertNotNull(response);
        assertEquals(
                List.of(
                        new WayblePathResponse.WayblePoint(37.1, 127.1, Type.NONE),
                        new WayblePathResponse.WayblePoint(37.2, 127.1, Type.RAMP),
                        new WayblePathResponse.WayblePoint(37.2, 127.2, Type.ELEVATOR),
                        new WayblePathResponse.WayblePoint(37.3, 127.2, Type.NONE)
                ), response.points()
        );
    }
}
