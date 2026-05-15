package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersResult;
import java.util.List;

public record SeedChallengersResponse(
    Long gisuId,
    int totalCreated,
    int totalFailed,
    List<PerCellSummary> perCellSummary
) {

    public record PerCellSummary(
        Long chapterId,
        Long schoolId,
        ChallengerPart part,
        int created,
        int failed
    ) {

        public static PerCellSummary from(SeedChallengersResult.PerCellSummary summary) {
            return new PerCellSummary(
                summary.chapterId(),
                summary.schoolId(),
                summary.part(),
                summary.created(),
                summary.failed()
            );
        }
    }

    public static SeedChallengersResponse from(SeedChallengersResult result) {
        return new SeedChallengersResponse(
            result.gisuId(),
            result.totalCreated(),
            result.totalFailed(),
            result.perCellSummary().stream().map(PerCellSummary::from).toList()
        );
    }
}
