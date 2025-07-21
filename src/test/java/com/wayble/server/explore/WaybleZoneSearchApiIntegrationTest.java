package com.wayble.server.explore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.entity.Address;
import com.wayble.server.explore.dto.search.WaybleZoneDocumentRegisterDto;
import com.wayble.server.explore.dto.search.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.UserType;
import com.wayble.server.user.repository.UserRepository;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.offset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class WaybleZoneSearchApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WaybleZoneDocumentRepository waybleZoneDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private static final double LATITUDE = 37.495;

    private static final double LONGITUDE = 127.045;

    private static final double RADIUS = 50.0;

    private static final String baseUrl = "/api/v1/wayble-zones/search";

    private Long userId;

    private String token;

    List<String> nameList = new ArrayList<>(Arrays.asList(
            "던킨도너츠",
            "베스킨라빈스",
            "투썸플레이스",
            "스타벅스",
            "메가엠지씨커피",
            "공차",
            "롯데리아",
            "맥도날드",
            "KFC",
            "노브랜드버거"
    ));

    @BeforeAll
    public void setup() {
        User testUser = User.createUser(
                "testUser", "testUsername", UUID.randomUUID() + "@email", "password",
                generateRandomBirthDate(), Gender.MALE, LoginType.KAKAO, UserType.DISABLED
        );

        userRepository.save(testUser);
        userId = testUser.getId();
        token = jwtTokenProvider.generateToken(userId, "ROLE_USER");

        for (int i = 1; i <= 1000; i++) {
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
                    .zoneName(nameList.get((int) (Math.random() * nameList.size())))
                    .address(address)
                    .waybleZoneType(WaybleZoneType.values()[i % WaybleZoneType.values().length])
                    .averageRating(Math.random() * 5)
                    .reviewCount((long)(Math.random() * 500))
                    .build();

            WaybleZoneDocument waybleZoneDocument = WaybleZoneDocument.fromDto(dto);
            waybleZoneDocumentRepository.save(waybleZoneDocument);
        }
    }

    @AfterAll
    public void teardown() {
        waybleZoneDocumentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void checkDataExists() {
        List<WaybleZoneDocument> all = waybleZoneDocumentRepository.findAll();
        System.out.println("=== 저장된 데이터 확인 ===");
        System.out.println("Total documents: " + all.size());

        assertThat(all.size()).isGreaterThan(0);
        for (WaybleZoneDocument doc : all) {
            assertThat(doc.getZoneId()).isNotNull();
            assertThat(doc.getZoneName()).isNotNull();
            assertThat(doc.getAddress().getLocation()).isNotNull();
            System.out.println("존 정보: " + doc.toString());
            System.out.println("주소: " + doc.getAddress().toString());
        }
    }

    @Test
    @DisplayName("좌표를 전달받아 반경 이내의 웨이블 존을 거리 순으로 조회")
    public void findWaybleZoneByDistanceAscending() throws Exception{
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("latitude",  String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("radiusKm",  String.valueOf(RADIUS))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode node = root.get("data");
        JsonNode dataNode = node.get("content");

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);
            double expected = haversine(LATITUDE, LONGITUDE,
                    dto.latitude(), dto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            dto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
        }

        for (WaybleZoneSearchResponseDto dto : dtoList) {
            System.out.println(dto.toString());
        }
    }

    @Test
    @DisplayName("특정 단어가 포함된 웨이블존을 거리 순으로 반환")
    public void findWaybleZoneByNameAscending() throws Exception{
        final String word = nameList.get((int) (Math.random() * nameList.size())).substring(0, 2);
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("latitude",  String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("radiusKm",  String.valueOf(RADIUS))
                        .param("zoneName",      word)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode node = root.get("data");
        JsonNode dataNode = node.get("content");

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);

            assertThat(dto.zoneName().contains(word)).isTrue();
            double expected = haversine(LATITUDE, LONGITUDE,
                    dto.latitude(), dto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            dto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
        }

        for (WaybleZoneSearchResponseDto dto : dtoList) {
            System.out.println(dto.toString());
        }
    }

    @Test
    @DisplayName("특정 타입의 웨이블존을 거리 순으로 반환")
    public void findWaybleZoneByZoneTypeAscending() throws Exception{
        final WaybleZoneType zoneType = WaybleZoneType.CAFE;
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("latitude",  String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("radiusKm",  String.valueOf(RADIUS))
                        .param("zoneType",  zoneType.name())
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode node = root.get("data");
        JsonNode dataNode = node.get("content");

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);

            assertThat(dto.zoneType()).isEqualTo(zoneType);
            double expected = haversine(LATITUDE, LONGITUDE,
                    dto.latitude(), dto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            dto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
        }

        for (WaybleZoneSearchResponseDto dto : dtoList) {
            System.out.println(dto.toString());
        }
    }

    @Test
    @DisplayName("특정 단어가 포함된 특정 타입의 웨이블존을 거리 순으로 반환")
    public void findWaybleZoneByNameAndZoneTypeAscending() throws Exception{
        final String word = nameList.get((int) (Math.random() * nameList.size())).substring(0, 2);
        final WaybleZoneType zoneType = WaybleZoneType.CAFE;
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("latitude",  String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("radiusKm",  String.valueOf(RADIUS))
                        .param("zoneName", word)
                        .param("zoneType",  zoneType.name())
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode node = root.get("data");
        JsonNode dataNode = node.get("content");

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);

            assertThat(dto.zoneName().contains(word)).isTrue();
            assertThat(dto.zoneType()).isEqualTo(zoneType);
            double expected = haversine(LATITUDE, LONGITUDE,
                    dto.latitude(), dto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            dto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
        }

        for (WaybleZoneSearchResponseDto dto : dtoList) {
            System.out.println(dto.toString());
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

    private LocalDate generateRandomBirthDate() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusYears(90); // 90세
        LocalDate end = today.minusYears(10);   // 10세

        long daysBetween = ChronoUnit.DAYS.between(start, end);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween + 1);

        return start.plusDays(randomDays);
    }
}

