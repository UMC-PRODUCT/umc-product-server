package com.umc.product.test.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

/**
 * 챌린저 분포 시딩 결과. ADR-017 참조.
 */
public record SeedChallengersResult(
    Long gisuId,
    int totalCreated,
    int totalFailed,
    List<PerCellSummary> perCellSummary
) {

    /**
     * (Chapter, School, Part) 셀별 생성 결과.
     */
    public record PerCellSummary(
        Long chapterId,
        Long schoolId,
        ChallengerPart part,
        int created,
        int failed
    ) {
    }
}
