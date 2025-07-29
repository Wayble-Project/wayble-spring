package com.wayble.server.wayblezone.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaybleZoneSyncScheduler {

    private final WaybleZoneSyncService waybleZoneSyncService;

    @Scheduled(cron = "0 0 2 * * ?")    // 매일 새벽 2시 실행
    //@Scheduled(cron = "0 38 21 * * ?") // 매일 오후 9시 30분 실행 (테스트용)
    public void scheduleWaybleZoneSync() {
        log.info("WaybleZone 정기 동기화 시작");
        
        try {
            WaybleZoneSyncService.SyncResult result = waybleZoneSyncService.syncUnsyncedZones();
            
            if (result.isSuccessful()) {
                log.info("WaybleZone 정기 동기화 성공 - 동기화된 항목: {}, 소요시간: {}ms", 
                        result.getSyncedCount(), result.getDurationMs());
            } else {
                log.warn("WaybleZone 정기 동기화 부분 실패 - 성공: {}, 실패: {}, 소요시간: {}ms",
                        result.getSyncedCount(), result.getFailedCount(), result.getDurationMs());
            }
            
        } catch (Exception e) {
            log.error("WaybleZone 정기 동기화 중 예상치 못한 오류 발생", e);
        }
    }
}