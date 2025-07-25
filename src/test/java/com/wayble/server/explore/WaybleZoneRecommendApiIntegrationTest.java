package com.wayble.server.explore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.entity.Address;
import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.dto.recommend.WaybleZoneRecommendResponseDto;
import com.wayble.server.explore.dto.search.request.WaybleZoneDocumentRegisterDto;
import com.wayble.server.common.entity.AgeGroup;
import com.wayble.server.explore.entity.RecommendLogDocument;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.repository.RecommendLogDocumentRepository;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
import com.wayble.server.explore.repository.recommend.WaybleZoneQueryRecommendRepository;
import com.wayble.server.user.entity.Gender;
import com.wayble.server.user.entity.LoginType;
import com.wayble.server.user.entity.User;
import com.wayble.server.user.entity.UserType;
import com.wayble.server.user.repository.UserRepository;
import com.wayble.server.wayblezone.entity.WaybleZoneFacility;
import com.wayble.server.wayblezone.entity.WaybleZoneType;
import com.wayble.server.wayblezone.entity.WaybleZoneVisitLog;
import com.wayble.server.wayblezone.repository.WaybleZoneVisitLogRepository;
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
    private WaybleZoneVisitLogRepository waybleZoneVisitLogRepository;

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
                "testUser", "testUsername", UUID.randomUUID() + "@email", "password",
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

            WaybleZoneFacility facility = createRandomFacility(i);
            
            WaybleZoneDocumentRegisterDto dto = WaybleZoneDocumentRegisterDto
                    .builder()
                    .zoneId((long) i)
                    .zoneName(nameList.get((int) (Math.random() * nameList.size())))
                    .address(address)
                    .thumbnailImageUrl("thumbnail url" + i)
                    .waybleZoneType(WaybleZoneType.values()[i % WaybleZoneType.values().length])
                    .facility(facility)
                    .averageRating(Math.random() * 5)
                    .reviewCount((long)(Math.random() * 500))
                    .build();

            WaybleZoneDocument waybleZoneDocument = WaybleZoneDocument.fromDto(dto);
            waybleZoneDocumentRepository.save(waybleZoneDocument);

            User user = User.createUser(
                    "user" + i,
                    "username" + i,
                    UUID.randomUUID() + "@email",
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
                WaybleZoneVisitLog visitLogDocument = WaybleZoneVisitLog
                        .builder()
                        .userId(user.getId())
                        .zoneId(zoneId)
                        .ageGroup(AgeGroup.fromBirthDate(user.getBirthDate()))
                        .gender(user.getGender())
                        .visitedAt(makeRandomDate())
                        .build();

                waybleZoneVisitLogRepository.save(visitLogDocument);
            }
        }
    }

    @AfterAll
    public void teardown() {
        waybleZoneDocumentRepository.deleteAll();
        waybleZoneVisitLogRepository.deleteAll();
        recommendLogDocumentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("데이터 저장 테스트")
    public void checkDataExists() {
        List<WaybleZoneDocument> waybleZoneDocumentList = waybleZoneDocumentRepository.findAll();
        assertThat(waybleZoneDocumentList.size()).isGreaterThan(0);
        System.out.println("Total documents: " + waybleZoneDocumentList.size());
        for (WaybleZoneDocument doc : waybleZoneDocumentList) {
            assertThat(doc.getZoneId()).isNotNull();
            assertThat(doc.getZoneName()).isNotNull();
            assertThat(doc.getAddress().getLocation()).isNotNull();
        }

        List<WaybleZoneVisitLog> waybleZoneVisitLogList = waybleZoneVisitLogRepository.findAll();
        assertThat(waybleZoneVisitLogList.size()).isGreaterThan(0);
        System.out.println("visit log size: " + waybleZoneVisitLogList.size());
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

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneRecommendResponseDto> waybleZoneRecommendResponseDtoList = objectMapper.convertValue(
                dataNode,
                new TypeReference<>() {}
        );

        assertThat(waybleZoneRecommendResponseDtoList.size()).isGreaterThan(0);

        WaybleZoneRecommendResponseDto dto = waybleZoneRecommendResponseDtoList.get(0);
        Long zoneId = dto.waybleZoneInfo().zoneId();

        Optional<RecommendLogDocument> recommendLogDocument = recommendLogDocumentRepository.findByUserIdAndZoneId(userId, zoneId);
        assertThat(recommendLogDocument.isPresent()).isTrue();
        assertThat(recommendLogDocument.get().getUserId()).isEqualTo(userId);
        assertThat(recommendLogDocument.get().getZoneId()).isEqualTo(zoneId);
        assertThat(recommendLogDocument.get().getRecommendationDate()).isEqualTo(LocalDate.now());
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

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneRecommendResponseDto> WaybleZoneRecommendResponseDtoList = objectMapper.convertValue(
                dataNode,
                new TypeReference<>() {}
        );

        assertThat(WaybleZoneRecommendResponseDtoList.size()).isEqualTo(1);
        WaybleZoneRecommendResponseDto dto = WaybleZoneRecommendResponseDtoList.get(0);
        assertThat(dto.waybleZoneInfo().zoneId()).isNotNull();
        assertThat(dto.waybleZoneInfo().zoneName()).isNotNull();
        assertThat(dto.waybleZoneInfo().zoneType()).isNotNull();
        assertThat(dto.waybleZoneInfo().latitude()).isNotNull();
        assertThat(dto.waybleZoneInfo().longitude()).isNotNull();
        assertThat(dto.waybleZoneInfo().facility()).isNotNull();
        assertThat(dto.waybleZoneInfo().facility().hasSlope()).isNotNull();
        assertThat(dto.waybleZoneInfo().facility().hasNoDoorStep()).isNotNull();
        assertThat(dto.waybleZoneInfo().facility().hasElevator()).isNotNull();
        assertThat(dto.waybleZoneInfo().facility().hasTableSeat()).isNotNull();
        assertThat(dto.waybleZoneInfo().facility().hasDisabledToilet()).isNotNull();
        assertThat(dto.waybleZoneInfo().facility().floorInfo()).isNotNull();
    }

    @Test
    @DisplayName("추천 결과 상위 N개 값 테스트")
    public void recommendWaybleZoneTop20() throws Exception {
        MvcResult result = mockMvc.perform(get(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .param("userId", String.valueOf(userId))
                        .param("latitude", String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("size", String.valueOf(20))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.get("data");

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneRecommendResponseDto> waybleZoneRecommendResponseDtoList = objectMapper.convertValue(
                dataNode,
                new TypeReference<>() {}
        );

        assertThat(waybleZoneRecommendResponseDtoList.size()).isGreaterThan(0);
        for (int i = 0; i < waybleZoneRecommendResponseDtoList.size(); i++) {
            WaybleZoneRecommendResponseDto dto = waybleZoneRecommendResponseDtoList.get(i);
            WaybleZoneInfoResponseDto zoneInfoResponseDto = dto.waybleZoneInfo();
            assertThat(zoneInfoResponseDto.zoneId()).isNotNull();
            assertThat(zoneInfoResponseDto.zoneName()).isNotNull();
            assertThat(zoneInfoResponseDto.zoneType()).isNotNull();
            assertThat(zoneInfoResponseDto.latitude()).isNotNull();
            assertThat(zoneInfoResponseDto.longitude()).isNotNull();
            if (i > 0) {
                assertThat(waybleZoneRecommendResponseDtoList.get(i - 1).totalScore()).isGreaterThanOrEqualTo(dto.totalScore());
            }
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
    
    private WaybleZoneFacility createRandomFacility(int i) {
        Random random = new Random(i); // 시드 고정으로 재현 가능한 랜덤
        
        String[] floors = {"B1", "1층", "2층", "3층"};
        
        return WaybleZoneFacility.builder()
                .hasSlope(random.nextBoolean())
                .hasNoDoorStep(random.nextBoolean())
                .hasElevator(random.nextBoolean())
                .hasTableSeat(random.nextBoolean())
                .hasDisabledToilet(random.nextBoolean())
                .floorInfo(floors[random.nextInt(floors.length)])
                .build();
    }
}
