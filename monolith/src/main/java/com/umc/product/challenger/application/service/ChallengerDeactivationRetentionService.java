package com.umc.product.challenger.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.in.command.PurgeExpiredChallengerDeactivationsUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationPurgeResult;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengerDeactivationRetentionService implements PurgeExpiredChallengerDeactivationsUseCase {

    static final int RETENTION_YEARS = 5;
    static final int BATCH_SIZE = 100;
    static final ZoneId RETENTION_ZONE = ZoneId.of("Asia/Seoul");

    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;
    private final DeleteChallengerDeactivationDataUseCase deleteChallengerDeactivationDataUseCase;
    private final Clock clock;

    static Instant retentionThreshold(Instant now) {
        return now.atZone(RETENTION_ZONE).minusYears(RETENTION_YEARS).toInstant();
    }

    @Override
    public ChallengerDeactivationPurgeResult purgeExpired() {
        Instant threshold = retentionThreshold(clock.instant());
        long afterId = 0L;
        int deletedCount = 0;
        int failedCount = 0;

        while (true) {
            List<ChallengerDeactivationRetentionTarget> targets = loadChallengerDeactivationPort
                .listRetentionTargets(threshold, afterId, BATCH_SIZE);
            if (targets.isEmpty()) {
                break;
            }

            for (ChallengerDeactivationRetentionTarget target : targets) {
                afterId = target.deactivationId();
                try {
                    deleteChallengerDeactivationDataUseCase.delete(target.deactivationId());
                    deletedCount++;
                } catch (RuntimeException exception) {
                    failedCount++;
                    log.error(
                        "탈부 보관 만료 기록 삭제에 실패했습니다: deactivationId={}, errorClass={}",
                        target.deactivationId(),
                        exception.getClass().getSimpleName(),
                        exception
                    );
                }
            }

            if (targets.size() < BATCH_SIZE) {
                break;
            }
        }

        return new ChallengerDeactivationPurgeResult(deletedCount, failedCount);
    }
}
