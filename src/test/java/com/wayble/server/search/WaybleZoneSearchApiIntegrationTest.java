package com.wayble.server.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.entity.Address;
import com.wayble.server.search.dto.WaybleZoneDocumentRegisterDto;
import com.wayble.server.search.entity.WaybleZoneDocument;
import com.wayble.server.search.repository.WaybleZoneSearchRepository;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WaybleZoneSearchApiIntegrationTest {

    @Autowired
    private WaybleZoneSearchRepository waybleZoneSearchRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        for (int i = 1; i <= 100; i++) {
            Map<String, Double> points = makeRandomPoint();
            Address address = Address.builder()
                    .state("state" + i)
                    .city("city" + i)
                    .district("district" + i)
                    .streetAddress("street address" + i)
                    .detailAddress("detail address" + i)
                    .latitude(points.get("latitude"))
                    .longitude(points.get("longitude"))
                    .build();

            WaybleZoneDocumentRegisterDto dto = WaybleZoneDocumentRegisterDto
                    .builder()
                    .zoneId((long) i)
                    .zoneName("waybleZone" + i)
                    .address(address)
                    .waybleZoneType(WaybleZoneType.values()[i % WaybleZoneType.values().length])
                    .averageRating(Math.random() * 5)
                    .reviewCount((long)(Math.random() * 500))
                    .build();

            WaybleZoneDocument waybleZoneDocument = WaybleZoneDocument.fromDto(dto);
            waybleZoneSearchRepository.save(waybleZoneDocument);
        }
    }

    @AfterEach
    public void teardown() {
        waybleZoneSearchRepository.deleteAll();
    }

    @Test
    public void test() {
        List<WaybleZoneDocument> waybleZoneList = waybleZoneSearchRepository.findAll();
        waybleZoneList.forEach(waybleZoneDocument -> {
            System.out.println(waybleZoneDocument.toString());
            System.out.println(waybleZoneDocument.getAddress().toString());
        });
    }

    private Map<String, Double> makeRandomPoint() {
        double centerLat = 37.495;   // 구 중심 위도
        double centerLng = 127.045;  // 구 중심 경도
        double radiusKm  = 150.0;      // 최대 반경

        double radiusDeg = radiusKm / 111.0;

        Random rnd = new Random();

        double u = rnd.nextDouble();
        double v = rnd.nextDouble();
        double w = radiusDeg * Math.sqrt(u);
        double t = 2 * Math.PI * v;

        double latOffset = w * Math.cos(t);
        double lngOffset = w * Math.sin(t) / Math.cos(Math.toRadians(centerLat));

        double randomLat = centerLat + latOffset;
        double randomLng = centerLng + lngOffset;

        return Map.of("latitude", randomLat, "longitude", randomLng);
    }
}

