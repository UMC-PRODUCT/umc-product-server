package com.umc.product.analytics.adapter.in.web.dto.response;

import java.util.List;
import java.util.Map;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSchoolsInfo;
import com.umc.product.common.domain.enums.ChallengerPart;

import lombok.Builder;

@Builder
public record AdminOperationsSchoolsResponse(List<ChapterStatusResponse> chapters) {

    public static AdminOperationsSchoolsResponse from(AdminOperationsSchoolsInfo info) {
        return AdminOperationsSchoolsResponse.builder()
            .chapters(info.chapters().stream()
                .map(ChapterStatusResponse::from)
                .toList())
            .build();
    }

    @Builder
    public record ChapterStatusResponse(
        Long chapterId,
        String chapterName,
        List<SchoolChallengerStatusResponse> schools
    ) {

        public static ChapterStatusResponse from(AdminOperationsSchoolsInfo.ChapterStatusInfo info) {
            return ChapterStatusResponse.builder()
                .chapterId(info.chapterId())
                .chapterName(info.chapterName())
                .schools(info.schools().stream()
                    .map(SchoolChallengerStatusResponse::from)
                    .toList())
                .build();
        }
    }

    @Builder
    public record SchoolChallengerStatusResponse(
        Long schoolId,
        String schoolName,
        long totalChallengerCount,
        Map<ChallengerPart, Long> challengerPartCounts
    ) {

        public static SchoolChallengerStatusResponse from(AdminOperationsSchoolsInfo.SchoolChallengerStatusInfo info) {
            return SchoolChallengerStatusResponse.builder()
                .schoolId(info.schoolId())
                .schoolName(info.schoolName())
                .totalChallengerCount(info.totalChallengerCount())
                .challengerPartCounts(info.challengerPartCounts())
                .build();
        }
    }
}
