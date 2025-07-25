package com.wayble.server.explore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayble.server.common.config.security.jwt.JwtTokenProvider;
import com.wayble.server.common.entity.Address;
import com.wayble.server.explore.dto.common.WaybleZoneInfoResponseDto;
import com.wayble.server.explore.dto.search.request.WaybleZoneDocumentRegisterDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneSearchResponseDto;
import com.wayble.server.explore.dto.search.response.WaybleZoneDistrictResponseDto;
import com.wayble.server.common.entity.AgeGroup;
import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
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
    private WaybleZoneVisitLogRepository waybleZoneVisitLogRepository;

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

    private static final int SAMPLES = 100;

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

    List<String> districtList = new ArrayList<>(Arrays.asList(
            "반포동",
            "잠원동",
            "서초동",
            "양재동",
            "내곡동"
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

        for (int i = 1; i <= SAMPLES; i++) {
            Map<String, Double> points = makeRandomPoint();
            Address address = Address.builder()
                    .state("state" + i)
                    .city("city" + i)
                    .district(districtList.get((int) (Math.random() * districtList.size())))
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
                    .waybleZoneType(WaybleZoneType.values()[i % WaybleZoneType.values().length])
                    .facility(facility)
                    .averageRating(Math.random() * 5)
                    .reviewCount((long)(Math.random() * 500))
                    .build();

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
                WaybleZoneVisitLog waybleZoneVisitLog = WaybleZoneVisitLog
                        .builder()
                        .userId(user.getId())
                        .zoneId(zoneId)
                        .ageGroup(AgeGroup.fromBirthDate(user.getBirthDate()))
                        .gender(user.getGender())
                        .visitedAt(makeRandomDate())
                        .build();

                waybleZoneVisitLogRepository.save(waybleZoneVisitLog);
            }

            WaybleZoneDocument waybleZoneDocument = WaybleZoneDocument.fromDto(dto);
            waybleZoneDocumentRepository.save(waybleZoneDocument);
        }
    }

    @AfterAll
    public void teardown() {
        waybleZoneVisitLogRepository.deleteAll();
        waybleZoneDocumentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void checkDataExists() {
        List<WaybleZoneDocument> all = waybleZoneDocumentRepository.findAll();


        assertThat(all.size()).isGreaterThan(0);
        System.out.println("Total documents: " + all.size());
        for (WaybleZoneDocument doc : all) {
            assertThat(doc.getZoneId()).isNotNull();
            assertThat(doc.getZoneName()).isNotNull();
            assertThat(doc.getAddress().getLocation()).isNotNull();
        }
    }

    @Test
    @DisplayName("좌표를 전달받아 반경 이내의 웨이블 존을 거리 순으로 조회")
    public void findWaybleZoneByDistanceAscending() throws Exception{
        MvcResult result = mockMvc.perform(get(baseUrl + "/maps")
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

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);
            WaybleZoneInfoResponseDto infoResponseDto = dto.waybleZoneInfo();
            double expected = haversine(LATITUDE, LONGITUDE,
                    infoResponseDto.latitude(), infoResponseDto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            infoResponseDto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
            
            // facility 검증 추가
            assertThat(infoResponseDto.facility()).isNotNull();
            assertThat(infoResponseDto.facility().hasSlope()).isNotNull();
            assertThat(infoResponseDto.facility().hasNoDoorStep()).isNotNull();
            assertThat(infoResponseDto.facility().hasElevator()).isNotNull();
            assertThat(infoResponseDto.facility().hasTableSeat()).isNotNull();
            assertThat(infoResponseDto.facility().hasDisabledToilet()).isNotNull();
            assertThat(infoResponseDto.facility().floorInfo()).isNotNull();
        }
    }

    @Test
    @DisplayName("특정 단어가 포함된 웨이블존을 거리 순으로 반환")
    public void findWaybleZoneByNameAscending() throws Exception{
        final String word = nameList.get((int) (Math.random() * nameList.size())).substring(0, 2);
        MvcResult result = mockMvc.perform(get(baseUrl + "/maps")
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

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);
            WaybleZoneInfoResponseDto infoResponseDto = dto.waybleZoneInfo();

            assertThat(infoResponseDto.zoneName().contains(word)).isTrue();
            double expected = haversine(LATITUDE, LONGITUDE,
                    infoResponseDto.latitude(), infoResponseDto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            infoResponseDto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
        }
    }

    @Test
    @DisplayName("특정 타입의 웨이블존을 거리 순으로 반환")
    public void findWaybleZoneByZoneTypeAscending() throws Exception{
        final WaybleZoneType zoneType = WaybleZoneType.CAFE;
        MvcResult result = mockMvc.perform(get(baseUrl + "/maps")
                        .header("Authorization", "Bearer " + token)
                        .param("latitude",  String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("radiusKm",  String.valueOf(RADIUS))
                        .param("zoneType",  zoneType.name())
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode node = root.get("data");
        JsonNode dataNode = node.get("content");

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);
            WaybleZoneInfoResponseDto infoResponseDto = dto.waybleZoneInfo();

            assertThat(infoResponseDto.zoneType()).isEqualTo(zoneType);
            double expected = haversine(LATITUDE, LONGITUDE,
                    infoResponseDto.latitude(), infoResponseDto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            infoResponseDto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
        }
    }

    @Test
    @DisplayName("특정 단어가 포함된 특정 타입의 웨이블존을 거리 순으로 반환")
    public void findWaybleZoneByNameAndZoneTypeAscending() throws Exception{
        final String word = nameList.get((int) (Math.random() * nameList.size())).substring(0, 2);
        final WaybleZoneType zoneType = WaybleZoneType.CAFE;
        MvcResult result = mockMvc.perform(get(baseUrl + "/maps")
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

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode node = root.get("data");
        JsonNode dataNode = node.get("content");

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneSearchResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        assertThat(dtoList).isNotEmpty();
        for (int i = 0; i < dtoList.size(); i++) {
            WaybleZoneSearchResponseDto dto = dtoList.get(i);
            WaybleZoneInfoResponseDto infoResponseDto = dto.waybleZoneInfo();

            assertThat(infoResponseDto.zoneName().contains(word)).isTrue();
            assertThat(infoResponseDto.zoneType()).isEqualTo(zoneType);
            double expected = haversine(LATITUDE, LONGITUDE,
                    infoResponseDto.latitude(), infoResponseDto.longitude());
            // 허용 오차: 0.05 km (≈50m)
            assertThat(dto.distance())
                    .withFailMessage("zoneId=%d: expected=%.5f, actual=%.5f",
                            infoResponseDto.zoneId(), expected, dto.distance())
                    .isCloseTo(expected, offset(0.05));

            if (i > 0) {
                assertThat(dto.distance())
                        .withFailMessage("거리 정렬 오류: %f !> %f",
                                dto.distance(), dtoList.get(i-1).distance())
                        .isGreaterThanOrEqualTo(dtoList.get(i - 1).distance());
            }
        }
    }

    @Test
    @DisplayName("특정 동 주변 Top3 웨이블존 검색순 기반 검색")
    public void findMostSearchesWaybleZoneByDistrict() throws Exception{
        final String district = districtList.get((int) (Math.random() * districtList.size()));
        MvcResult result = mockMvc.perform(get(baseUrl + "/district/most-searches")
                        .header("Authorization", "Bearer " + token)
                        .param("latitude",  String.valueOf(LATITUDE))
                        .param("longitude", String.valueOf(LONGITUDE))
                        .param("district", district)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.get("data");

        System.out.println("==== 응답 결과 ====");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(json)));

        List<WaybleZoneDistrictResponseDto> dtoList =
                objectMapper.convertValue(
                        dataNode,
                        new TypeReference<>() {}
                );

        // 검증: 결과가 비어있지 않아야 함 (최대 3개)
        assertThat(dtoList).isNotEmpty();
        assertThat(dtoList.size()).isLessThanOrEqualTo(3);

        // 검증: 각 결과의 필수 필드들이 존재하는지 확인
        for (WaybleZoneDistrictResponseDto dto : dtoList) {
            assertThat(dto.visitCount()).isNotNull();
            assertThat(dto.visitCount()).isGreaterThan(0L);
            
            // 필수 필드들이 존재하는지 확인
            assertThat(dto.waybleZoneInfo().zoneId()).isNotNull();
            assertThat(dto.waybleZoneInfo().zoneName()).isNotNull();
            assertThat(dto.waybleZoneInfo().zoneType()).isNotNull();
            assertThat(dto.waybleZoneInfo().latitude()).isNotNull();
            assertThat(dto.waybleZoneInfo().longitude()).isNotNull();
        }

        // 검증: 방문 수 내림차순으로 정렬되어야 함
        for (int i = 1; i < dtoList.size(); i++) {
            assertThat(dtoList.get(i).visitCount())
                    .withFailMessage("방문 수 정렬 오류: 인덱스 %d의 방문 수(%d)가 인덱스 %d의 방문 수(%d)보다 크면 안됩니다",
                            i, dtoList.get(i).visitCount(), i-1, dtoList.get(i-1).visitCount())
                    .isLessThanOrEqualTo(dtoList.get(i-1).visitCount());
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

