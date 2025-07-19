package com.wayble.server.explore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.entity.Address;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.dto.search.WaybleZoneDocumentRegisterDto;
import com.wayble.server.explore.entity.AgeGroup;
import com.wayble.server.explore.entity.RecommendLogDocument;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.entity.WaybleZoneVisitLogDocument;
import com.wayble.server.explore.repository.RecommendLogDocumentRepository;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
import com.wayble.server.explore.repository.WaybleZoneVisitLogDocumentRepository;
import com.wayble.server.explore.repository.recommend.WaybleZoneQueryRecommendRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class WaybleZoneRecommendApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WaybleZoneQueryRecommendRepository waybleZoneRecommendRepository;

    @Autowired
    private WaybleZoneDocumentRepository waybleZoneDocumentRepository;

    @Autowired
    private WaybleZoneVisitLogDocumentRepository waybleZoneVisitLogDocumentRepository;

    @Autowired
    private RecommendLogDocumentRepository recommendLogDocumentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final double LATITUDE = 37.495;

    private static final double LONGITUDE = 127.045;

    private static final double RADIUS = 50.0;

    private static final Long SAMPLES = 100L;

    private static final String baseUrl = "/api/v1/wayble-zones/recommend";

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
                "testUser", "testUsername", "test@email.com", "password",
                generateRandomBirthDate(), Gender.MALE, LoginType.KAKAO, UserType.DISABLED
        );
        userRepository.save(testUser);
        userId = testUser.getId();
        token = jwtTokenProvider.generateToken(userId, "ROLE_USER");

        for (int i = 1; i <= SAMPLES / 2; i++) {
            Long zoneId = (long) (Math.random() * SAMPLES) + 1;
            if(!recommendLogDocumentRepository.existsByUserIdAndZoneId(userId, zoneId)) {
                RecommendLogDocument recommendLogDocument = RecommendLogDocument
                        .builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .zoneId(zoneId)
                        .recommendationDate(makeRandomDate())
                        .recommendCount(1L)
                        .build();

                recommendLogDocumentRepository.save(recommendLogDocument);
            }
        }

        for (int i = 1; i <= SAMPLES; i++) {
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
                    .thumbnailImageUrl("thumbnail url" + i)
                    .waybleZoneType(WaybleZoneType.values()[i % WaybleZoneType.values().length])
                    .averageRating(Math.random() * 5)
                    .reviewCount((long)(Math.random() * 500))
                    .build();

            WaybleZoneDocument waybleZoneDocument = WaybleZoneDocument.fromDto(dto);
            waybleZoneDocumentRepository.save(waybleZoneDocument);

            User user = User.createUser(
                    "user" + i,
                    "username" + i,
                    "user" + i + "@email",
                    "password" + i,
                    generateRandomBirthDate(),
                    Gender.values()[i % 2],
                    LoginType.values()[i % LoginType.values().length],
                    UserType.DISABLED
            );
            userRepository.save(user);

            int count = (int) (Math.random() * 30) + 1;
            for (int j = 0; j < count; j++) {
                Long zoneId = (long) (Math.random() * SAMPLES) + 1;
                WaybleZoneVisitLogDocument visitLogDocument = WaybleZoneVisitLogDocument
                        .builder()
                        .userId(user.getId())
                        .zoneId(zoneId)
                        .ageGroup(AgeGroup.fromBirthDate(user.getBirthDate()))
                        .gender(user.getGender())
                        .build();

                waybleZoneVisitLogDocumentRepository.save(visitLogDocument);
            }
        }
    }

    @AfterAll
    public void teardown() {
        waybleZoneDocumentRepository.deleteAll();
        waybleZoneVisitLogDocumentRepository.deleteAll();
        recommendLogDocumentRepository.deleteAll();
        userRepository.deleteAll();
        //SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @DisplayName("데이터 저장 테스트")
    public void checkDataExists() {
        List<WaybleZoneDocument> waybleZoneDocumentList = waybleZoneDocumentRepository.findAll();
        System.out.println("=== 웨이블존 목록 ===");

        assertThat(waybleZoneDocumentList.size()).isGreaterThan(0);
        for (WaybleZoneDocument doc : waybleZoneDocumentList) {
            assertThat(doc.getZoneId()).isNotNull();
            assertThat(doc.getZoneName()).isNotNull();
            assertThat(doc.getAddress().getLocation()).isNotNull();
            System.out.println("존 정보: " + doc.toString());
            System.out.println("주소: " + doc.getAddress().toString());
        }

        List<WaybleZoneVisitLogDocument> waybleZoneVisitLogList = waybleZoneVisitLogDocumentRepository.findAll();
        System.out.println("=== 웨이블존 방문 목록 ===");

        assertThat(waybleZoneVisitLogList.size()).isGreaterThan(0);
        for (WaybleZoneVisitLogDocument doc : waybleZoneVisitLogList) {
            System.out.println("방문 정보" + doc.toString());
        }
    }

    @Test
    @DisplayName("추천 기록 저장 테스트")
    public void saveRecommendLogTest() throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("userId", String.valueOf(userId))
                        .param("latitude", String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.get("data");

        List<WaybleZoneRecommendResponseDto> waybleZoneRecommendResponseDtoList = objectMapper.convertValue(
                dataNode,
                new TypeReference<>() {}
        );

        assertThat(waybleZoneRecommendResponseDtoList.size()).isGreaterThan(0);

        WaybleZoneRecommendResponseDto dto = waybleZoneRecommendResponseDtoList.get(0);
        Long zoneId = dto.zoneId();

        Optional<RecommendLogDocument> recommendLogDocument = recommendLogDocumentRepository.findByUserIdAndZoneId(userId, zoneId);
        assertThat(recommendLogDocument.isPresent()).isTrue();
        assertThat(recommendLogDocument.get().getUserId()).isEqualTo(userId);
        assertThat(recommendLogDocument.get().getZoneId()).isEqualTo(zoneId);
        assertThat(recommendLogDocument.get().getRecommendationDate()).isEqualTo(LocalDate.now());
        System.out.println("===recommend log===");
        System.out.println("id = " + recommendLogDocument.get().getId());
        System.out.println("userId = " +recommendLogDocument.get().getUserId());
        System.out.println("zoneId = " +recommendLogDocument.get().getZoneId());
        System.out.println("recommendationDate = " +recommendLogDocument.get().getRecommendationDate());
        System.out.println("recommendCount " +recommendLogDocument.get().getRecommendCount());
    }

    @Test
    @DisplayName("추천 기능 테스트")
    public void recommendWaybleZone() throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("userId", String.valueOf(userId))
                        .param("latitude", String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.get("data");

        List<WaybleZoneRecommendResponseDto> WaybleZoneRecommendResponseDtoList = objectMapper.convertValue(
                dataNode,
                new TypeReference<>() {}
        );

        assertThat(WaybleZoneRecommendResponseDtoList.size()).isEqualTo(1);
        WaybleZoneRecommendResponseDto dto = WaybleZoneRecommendResponseDtoList.get(0);
        assertThat(dto.zoneId()).isNotNull();
        assertThat(dto.zoneName()).isNotNull();
        assertThat(dto.zoneType()).isNotNull();
        assertThat(dto.latitude()).isNotNull();
        assertThat(dto.longitude()).isNotNull();

        System.out.println("zoneId = " + dto.zoneId());
        System.out.println("zoneName = " + dto.zoneName());
        System.out.println("zoneType = " + dto.zoneType());
        System.out.println("thumbnailImageUrl = " + dto.thumbnailImageUrl());
        System.out.println("latitude = " + dto.latitude());
        System.out.println("longitude = " + dto.longitude());
        System.out.println("rating = " + dto.averageRating());
        System.out.println("reviewCount = " + dto.reviewCount());
        System.out.println("distance = " + haversine(dto.latitude(), dto.longitude(), LATITUDE, LONGITUDE));
        System.out.println("distanceScore = " + dto.distanceScore());
        System.out.println("similarityScore = " + dto.similarityScore());
        System.out.println("recencyScore = " + dto.recencyScore());
        System.out.println("totalScore = " + dto.totalScore());
    }

    @Test
    @DisplayName("추천 결과 상위 N개 값 테스트")
    public void recommendWaybleZoneTop20() throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("userId", String.valueOf(userId))
                        .param("latitude", String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("count", String.valueOf(20))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.get("data");

        List<WaybleZoneRecommendResponseDto> waybleZoneRecommendResponseDtoList = objectMapper.convertValue(
                dataNode,
                new TypeReference<>() {}
        );

        assertThat(waybleZoneRecommendResponseDtoList.size()).isGreaterThan(0);
        for (int i = 0; i < waybleZoneRecommendResponseDtoList.size(); i++) {
            WaybleZoneRecommendResponseDto dto = waybleZoneRecommendResponseDtoList.get(i);
            assertThat(dto.zoneId()).isNotNull();
            assertThat(dto.zoneName()).isNotNull();
            assertThat(dto.zoneType()).isNotNull();
            assertThat(dto.latitude()).isNotNull();
            assertThat(dto.longitude()).isNotNull();
            if (i > 0) {
                assertThat(waybleZoneRecommendResponseDtoList.get(i - 1).totalScore()).isGreaterThanOrEqualTo(dto.totalScore());
            }

            System.out.println("zoneId = " + dto.zoneId());
            System.out.println("zoneName = " + dto.zoneName());
            System.out.println("zoneType = " + dto.zoneType());
            System.out.println("thumbnailImageUrl = " + dto.thumbnailImageUrl());
            System.out.println("latitude = " + dto.latitude());
            System.out.println("longitude = " + dto.longitude());
            System.out.println("rating = " + dto.averageRating());
            System.out.println("reviewCount = " + dto.reviewCount());
            System.out.println("distance = " + haversine(dto.latitude(), dto.longitude(), LATITUDE, LONGITUDE));
            System.out.println("distanceScore = " + dto.distanceScore());
            System.out.println("similarityScore = " + dto.similarityScore());
            System.out.println("recencyScore = " + dto.recencyScore());
            System.out.println("totalScore = " + dto.totalScore());
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

    private LocalDate makeRandomDate() {
        Random random = new Random();
        int daysAgo = random.nextInt(40) + 1;
        return LocalDate.now().minusDays(daysAgo);
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
