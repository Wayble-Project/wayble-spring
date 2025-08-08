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
                    // --- CSV 컬럼 인덱스 매핑 ---
                    String name        = nz(r.get(0));   // 상호명
                    String branch      = nz(r.get(1));   // 지점명
                    String mid         = nz(r.get(4));   // 상권업종중분류명
                    String small       = nz(r.get(6));   // 상권업종소분류명
                    String state       = nz(r.get(8));   // 시도명
                    String city        = nz(r.get(10));  // 시군구명
                    String district    = nz(r.get(12));  // 법정동명
                    String street      = nz(r.get(13));  // 도로명주소
                    String phone       = nz(r.get(14));  // 전화번호
                    String openHourRaw = nz(r.get(15));  // 영업시간
                    String lat         = nz(r.get(16));  // 위도
                    String lon         = nz(r.get(17));  // 경도
                    String firstFloor  = nz(r.get(18));  // 일층 (Y/N)
                    String slope       = nz(r.get(19));  // 경사로 (Y/N)
                    String entranceStep= nz(r.get(20));  // 입구턱 (Y/N)
                    String entranceDoorStep = nz(r.get(21)); // 입구문턱 (Y/N)
                    String table       = nz(r.get(22));  // 테이블석 (Y/N)
                    String disabledToilet = nz(r.get(25)); // 장애인화장실 (Y/N)
                    String elevator    = nz(r.get(26));  // 엘리베이터 (Y/N)

                    // 현재 웨이블존 서비스에서 지원하는 ZoneType만 가져오기
                    WaybleZoneType type = toZoneType(small, mid);
                    if (type == null) {
                        skip++;
                        continue;
                    }

                    // 가게명 조합
                    String zoneName = branch.isEmpty() ? name : name + " " + branch;
                    Address addr = toAddress(state, city, district, street, lat, lon);

                    // 웨이블존 저장
                    WaybleZone zone = WaybleZone.fromImporter(
                            zoneName,
                            phone.isBlank() ? null : phone,
                            type,
                            addr
                    );
                    zone = zoneRepo.save(zone);

                    // 문턱 여부 판단
                    boolean hasDoorStep = ynToBool(entranceStep) || ynToBool(entranceDoorStep);
                    boolean hasNoDoorStep = !hasDoorStep;

                    // 시설 저장
                    WaybleZoneFacility fac = WaybleZoneFacility.builder()
                            .waybleZone(zone)
                            .hasSlope(ynToBool(slope))
                            .hasNoDoorStep(hasNoDoorStep)
                            .hasElevator(ynToBool(elevator))
                            .hasTableSeat(ynToBool(table))
                            .hasDisabledToilet(ynToBool(disabledToilet))
                            .floorInfo(ynToBool(firstFloor) ? "1층" : null)
                            .build();
                    facilityRepo.save(fac);

                    // 영업시간 저장
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