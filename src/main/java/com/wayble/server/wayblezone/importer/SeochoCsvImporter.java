package com.wayble.server.wayblezone.importer;

import com.wayble.server.common.entity.Address;
import com.wayble.server.wayblezone.entity.*;
import com.wayble.server.wayblezone.repository.WaybleZoneFacilityRepository;
import com.wayble.server.wayblezone.repository.WaybleZoneOperatingHourRepository;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;

import static com.wayble.server.wayblezone.importer.CsvSupport.*;

@Slf4j
@Profile("local")
@Component
@RequiredArgsConstructor
public class SeochoCsvImporter implements CommandLineRunner {

    private final WaybleZoneRepository zoneRepo;
    private final WaybleZoneFacilityRepository facilityRepo;
    private final WaybleZoneOperatingHourRepository hourRepo;

    @Value("${wayble.import.enabled:false}")
    private boolean enabled;

    @Value("${wayble.import.csv-path:}")
    private String csvPath;

    @Value("${wayble.import.skip-header:true}")
    private boolean skipHeader;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!enabled) {
            log.info("[Wayble Import] disabled. skip.");
            return;
        }
        log.info("[Wayble Import] start: {}", csvPath);

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath));
             CSVParser parser = CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withIgnoreEmptyLines()
                     .withTrim()
                     .parse(reader)) {

            int idx = 0, ok = 0, skip = 0, fail = 0;
            for (CSVRecord r : parser) {
                idx++;
                if (skipHeader && r.getRecordNumber() == 1) continue;

                try {
                    // --- CSV 컬럼 인덱스 매핑  ---
                    String name = nz(r.get(0));          // 상호명
                    String branch = nz(r.get(1));        // 지점명
                    String mid = nz(r.get(4));           // 상권업종중분류명
                    String small = nz(r.get(6));         // 상권업종소분류명
                    String state = nz(r.get(9-1));       // 시도명
                    String city  = nz(r.get(11-1));      // 시군구명
                    String district = nz(r.get(13-1));   // 법정동명
                    String street = nz(r.get(15-1));     // 도로명주소
                    String phone = nz(r.get(16-1));      // 전화번호
                    String openHourRaw = nz(r.get(17-1));// 영업시간
                    String lat = nz(r.get(18-1));        // 위도
                    String lon = nz(r.get(19-1));        // 경도
                    String firstFloor = nz(r.get(20-1)); // 일층 (Y/N)
                    String slope = nz(r.get(21-1));      // 경사로 (Y/N)
                    String doorStep = nz(r.get(23-1));   // 입구문턱 (Y/N)
                    String table = nz(r.get(25-1));      // 테이블석 (Y/N)
                    String toilet = nz(r.get(28-1));     // 장애인화장실 (Y/N)
                    String elevator = nz(r.get(29-1));   // 엘리베이터 (Y/N)

                    WaybleZoneType type = toZoneType(small, mid);
                    if (type == null) {
                        skip++;
                        continue; // 우리 타입 외는 스킵
                    }

                    String zoneName = (branch.isEmpty() ? name : name + " " + branch);
                    Address addr = toAddress(state, city, district, street, lat, lon);

                    WaybleZone zone = WaybleZone.builder()
                            .zoneName(zoneName)
                            .contactNumber(phone.isBlank() ? null : phone)
                            .zoneType(type)
                            .address(addr)
                            .rating(0.0)
                            .reviewCount(0)
                            .likes(0)
                            .mainImageUrl(null)
                            .build();

                    zone = zoneRepo.save(zone);

                    // 시설
                    WaybleZoneFacility fac = WaybleZoneFacility.builder()
                            .waybleZone(zone)
                            .hasSlope(ynToBool(slope))
                            .hasNoDoorStep(ynToBool(doorStep))      // 문턱 없음 여부로 저장
                            .hasElevator(ynToBool(elevator))
                            .hasTableSeat(ynToBool(table))
                            .hasDisabledToilet(ynToBool(toilet))
                            .floorInfo(ynToBool(firstFloor) ? "1층" : null)
                            .build();
                    facilityRepo.save(fac);

                    // 영업시간
                    Map<DayOfWeek, BusinessHourParser.TimeRange> hours =
                            new EnumMap<>(BusinessHourParser.parse(openHourRaw));
                    for (Map.Entry<DayOfWeek, BusinessHourParser.TimeRange> e : hours.entrySet()) {
                        WaybleZoneOperatingHour oh = WaybleZoneOperatingHour.builder()
                                .waybleZone(zone)
                                .dayOfWeek(e.getKey())
                                .startTime(e.getValue().open())
                                .closeTime(e.getValue().close())
                                .isClosed(false)
                                .build();
                        hourRepo.save(oh);
                    }

                    ok++;
                } catch (Exception e) {
                    fail++;
                    log.warn("[Wayble Import] row {} failed: {}", idx, e.toString());
                }
            }
            log.info("[Wayble Import] done. total={}, ok={}, skip(type)={}, fail={}", idx-1, ok, skip, fail);
        }
    }
}