package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsPointsInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import lombok.Builder;

@Builder
public record AdminOperationsPointsResponse(List<ChapterPartPointGrantStatusResponse> pointGrantStatuses) {

    public static AdminOperationsPointsResponse from(AdminOperationsPointsInfo info) {
        return AdminOperationsPointsResponse.builder()
            .pointGrantStatuses(info.pointGrantStatuses().stream()
                .map(ChapterPartPointGrantStatusResponse::from)
                .toList())
            .build();
    }

    @Builder
    public record ChapterPartPointGrantStatusResponse(
        Long chapterId,
        String chapterName,
        ChallengerPart part,
        long grantCount,
        double pointSum
    ) {

        public static ChapterPartPointGrantStatusResponse from(
            AdminOperationsPointsInfo.ChapterPartPointGrantStatusInfo info
        ) {
            return ChapterPartPointGrantStatusResponse.builder()
                .chapterId(info.chapterId())
                .chapterName(info.chapterName())
                .part(info.part())
                .grantCount(info.grantCount())
                .pointSum(info.pointSum())
                .build();
        }
    }
}
