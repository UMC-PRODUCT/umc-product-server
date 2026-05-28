package com.umc.product.analytics.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import lombok.Builder;

@Builder
public record AdminOperationsPointsInfo(List<ChapterPartPointGrantStatusInfo> pointGrantStatuses) {

    public static AdminOperationsPointsInfo from(List<ChapterPartPointGrantStatusInfo> pointGrantStatuses) {
        return AdminOperationsPointsInfo.builder()
            .pointGrantStatuses(List.copyOf(pointGrantStatuses))
            .build();
    }

    @Builder
    public record ChapterPartPointGrantStatusInfo(
        Long chapterId,
        String chapterName,
        ChallengerPart part,
        long grantCount,
        double pointSum
    ) {

        public static ChapterPartPointGrantStatusInfo of(
            Long chapterId,
            String chapterName,
            ChallengerPart part,
            long grantCount,
            double pointSum
        ) {
            return ChapterPartPointGrantStatusInfo.builder()
                .chapterId(chapterId)
                .chapterName(chapterName)
                .part(part)
                .grantCount(grantCount)
                .pointSum(pointSum)
                .build();
        }
    }
}
