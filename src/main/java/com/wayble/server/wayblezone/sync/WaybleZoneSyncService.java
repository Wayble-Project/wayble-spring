package com.wayble.server.wayblezone.sync;

import com.wayble.server.explore.entity.WaybleZoneDocument;
import com.wayble.server.explore.repository.WaybleZoneDocumentRepository;
import com.wayble.server.wayblezone.entity.WaybleZone;
import com.wayble.server.wayblezone.repository.WaybleZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaybleZoneSyncService {

    private final WaybleZoneRepository waybleZoneRepository;
    private final WaybleZoneDocumentRepository waybleZoneDocumentRepository;

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Transactional
    public SyncResult syncUnsyncedZones() {
        log.info("동기화되지 않은 WaybleZone 동기화 시작");
        
        int totalSynced = 0;
        int totalFailed = 0;
        LocalDateTime syncStartTime = LocalDateTime.now();
        
        List<WaybleZone> unsyncedZones;
        while (!(unsyncedZones = waybleZoneRepository.findUnsyncedZones(BATCH_SIZE)).isEmpty()) {
            log.info("동기화 대상 {} 개 조회", unsyncedZones.size());
            
            for (WaybleZone zone : unsyncedZones) {
                try {
                    syncSingleZone(zone);
                    totalSynced++;
                    log.debug("Zone {} 동기화 완료", zone.getId());
                } catch (Exception e) {
                    totalFailed++;
                    log.error("Zone {} 동기화 실패: {}", zone.getId(), e.getMessage(), e);
                }
            }
        }
        
        LocalDateTime syncEndTime = LocalDateTime.now();
        SyncResult result = new SyncResult(totalSynced, totalFailed, syncStartTime, syncEndTime);
        
        log.info("동기화 완료 - 성공: {}, 실패: {}, 소요시간: {}ms", 
                totalSynced, totalFailed, result.getDurationMs());
        
        return result;
    }

    @Transactional
    public SyncResult syncZonesModifiedAfter(LocalDateTime since) {
        log.info("{} 이후 수정된 WaybleZone 동기화 시작", since);
        
        int totalSynced = 0;
        int totalFailed = 0;
        LocalDateTime syncStartTime = LocalDateTime.now();
        
        List<WaybleZone> modifiedZones;
        while (!(modifiedZones = waybleZoneRepository.findZonesModifiedAfter(since, BATCH_SIZE)).isEmpty()) {
            log.info("동기화 대상 {} 개 조회", modifiedZones.size());
            
            for (WaybleZone zone : modifiedZones) {
                try {
                    syncSingleZone(zone);
                    totalSynced++;
                    log.debug("Zone {} 동기화 완료", zone.getId());
                } catch (Exception e) {
                    totalFailed++;
                    log.error("Zone {} 동기화 실패: {}", zone.getId(), e.getMessage(), e);
                }
            }
        }
        
        LocalDateTime syncEndTime = LocalDateTime.now();
        SyncResult result = new SyncResult(totalSynced, totalFailed, syncStartTime, syncEndTime);
        
        log.info("동기화 완료 - 성공: {}, 실패: {}, 소요시간: {}ms",
                totalSynced, totalFailed, result.getDurationMs());
        
        return result;
    }

    private void syncSingleZone(WaybleZone zone) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                attempts++;
                
                // WaybleZone을 WaybleZoneDocument로 변환하여 ES에 저장
                WaybleZoneDocument document = WaybleZoneDocument.fromEntity(zone);
                waybleZoneDocumentRepository.save(document);
                
                // MySQL에서 동기화 완료 표시
                zone.markAsSynced();
                waybleZoneRepository.save(zone);
                
                log.debug("Zone {} 동기화 성공 (시도: {})", zone.getId(), attempts);
                return;
                
            } catch (OptimisticLockingFailureException e) {
                lastException = e;
                log.warn("Zone {} 동기화 중 OptimisticLockingFailure 발생 (시도: {})", zone.getId(), attempts);
                
                if (attempts < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(100 * attempts); // 재시도 간격 증가
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("동기화 중 인터럽트 발생", ie);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.error("Zone {} 동기화 중 예상치 못한 오류 발생 (시도: {}): {}", 
                         zone.getId(), attempts, e.getMessage());
                break; // 재시도 불가능한 오류
            }
        }
        
        throw new RuntimeException(
            String.format("Zone %d 동기화 실패 (최대 재시도 횟수 초과)", zone.getId()), 
            lastException
        );
    }

    public static class SyncResult {
        private final int syncedCount;
        private final int failedCount;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;

        public SyncResult(int syncedCount, int failedCount, LocalDateTime startTime, LocalDateTime endTime) {
            this.syncedCount = syncedCount;
            this.failedCount = failedCount;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public int getSyncedCount() { return syncedCount; }
        public int getFailedCount() { return failedCount; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        
        public long getDurationMs() {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        
        public boolean isSuccessful() {
            return failedCount == 0;
        }
    }
}