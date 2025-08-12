package com.wayble.server.explore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.explore.entity.FacilityType;
import com.wayble.server.explore.entity.WaybleFacilityDocument;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.repository.facility.WaybleFacilityDocumentRepository;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.UserType;
import com.wayble.server.user.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class WaybleFacilityApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WaybleFacilityDocumentRepository waybleFacilityDocumentRepository;

    private static final double LATITUDE = 37.5435480;

    private static final double LONGITUDE = 126.9518410;

    private static final double RADIUS = 20.0;

    private static final int SAMPLES = 100;

    private static final String baseUrl = "/api/v1/wayble-zones/facilities";

    private Long userId;

    private String token;

    @BeforeAll
    public void setup() {
        User testUser = User.createUserWithDetails(
                "testUser", "testUsername", UUID.randomUUID() + "@email", "password",
                LocalDate.now(), Gender.MALE, LoginType.KAKAO, UserType.DISABLED
        );

        userRepository.save(testUser);
        userId = testUser.getId();
        token = jwtTokenProvider.generateToken(userId, "ROLE_USER");

        for(int i = 1; i <= SAMPLES; i++) {
            Map<String, Double> points = makeRandomPoint();

            WaybleFacilityDocument rampDocument = WaybleFacilityDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .location(new GeoPoint(points.get("latitude"), points.get("longitude")))
                    .facilityType(FacilityType.RAMP)
                    .build();

            WaybleFacilityDocument elevatorDocument = WaybleFacilityDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .location(new GeoPoint(points.get("latitude"), points.get("longitude")))
                    .facilityType(FacilityType.ELEVATOR)
                    .build();
            
            waybleFacilityDocumentRepository.save(rampDocument);
            waybleFacilityDocumentRepository.save(elevatorDocument);
        }
    }

    @AfterAll
    public void teardown() {
        waybleFacilityDocumentRepository.deleteAll();
        userRepository.deleteById(userId);
    }

    @Test
    public void checkDataExists() {
        List<WaybleFacilityDocument> all = waybleFacilityDocumentRepository.findAll();
        assertThat(all.size()).isGreaterThan(0);

        for (WaybleFacilityDocument doc : all) {
            assertThat(doc.getId()).isNotNull();
            assertThat(doc.getLocation()).isNotNull();
            assertThat(doc.getFacilityType()).isNotNull();
            System.out.println(doc);
        }
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6_371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private Map<String, Double> makeRandomPoint() {
        double radiusDeg = RADIUS / 111.0;

        Random rnd = new Random();

        double u = rnd.nextDouble();
        double v = rnd.nextDouble();
        double w = radiusDeg * Math.sqrt(u);
        double t = 2 * Math.PI * v;

        double latOffset = w * Math.cos(t);
        double lngOffset = w * Math.sin(t) / Math.cos(Math.toRadians(LATITUDE));

        double randomLat = LATITUDE + latOffset;
        double randomLng = LONGITUDE + lngOffset;

        return Map.of("latitude", randomLat, "longitude", randomLng);
    }
}
