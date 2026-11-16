package com.umc.product.challenger.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.umc.product.challenger.application.port.in.command.DeleteChallengerDeactivationDataUseCase;
import com.umc.product.challenger.application.port.in.command.PurgeExpiredChallengerDeactivationsUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationPurgeResult;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;
import com.umc.product.storage.application.port.in.command.DeleteStoredFileUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengerDeactivationRetentionService implements PurgeExpiredChallengerDeactivationsUseCase {

    static final int RETENTION_YEARS = 5;
    static final int BATCH_SIZE = 100;
    private static final ZoneId RETENTION_ZONE = ZoneId.of("Asia/Seoul");

    private final LoadChallengerDeactivationPort loadChallengerDeactivationPort;
    private final DeleteChallengerDeactivationDataUseCase deleteChallengerDeactivationDataUseCase;
    private final DeleteStoredFileUseCase deleteStoredFileUseCase;
    private final Clock clock;

    @Override
    public ChallengerDeactivationPurgeResult purgeExpired() {
        Instant threshold = ZonedDateTime.now(clock)
            .withZoneSameInstant(RETENTION_ZONE)
            .minusYears(RETENTION_YEARS)
            .toInstant();
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
                    deleteExclusiveEvidenceFiles(target);
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

    private void deleteExclusiveEvidenceFiles(ChallengerDeactivationRetentionTarget target) {
        target.imageIds().stream()
            .distinct()
            .filter(imageId -> !loadChallengerDeactivationPort.existsImageReferenceOutsideDeactivation(
                imageId,
                target.deactivationId()
            ))
            .forEach(deleteStoredFileUseCase::deleteIfExists);
    }
}
