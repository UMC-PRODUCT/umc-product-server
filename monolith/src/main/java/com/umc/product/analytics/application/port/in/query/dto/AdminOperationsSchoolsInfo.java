package com.umc.product.analytics.application.port.in.query.dto;

import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;

import lombok.Builder;

@Builder
public record AdminOperationsSchoolsInfo(
    List<ChapterStatusInfo> chapters
) {

    public static AdminOperationsSchoolsInfo from(List<ChapterStatusInfo> chapters) {
        return AdminOperationsSchoolsInfo.builder()
            .chapters(List.copyOf(chapters))
            .build();
    }

    @Builder
    public record ChapterStatusInfo(
        Long chapterId,
        String chapterName,
        List<SchoolChallengerStatusInfo> schools
    ) {

        public static ChapterStatusInfo of(
            Long chapterId,
            String chapterName,
            List<SchoolChallengerStatusInfo> schools
        ) {
            return ChapterStatusInfo.builder()
                .chapterId(chapterId)
                .chapterName(chapterName)
                .schools(List.copyOf(schools))
                .build();
        }
    }

    @Builder
    public record SchoolChallengerStatusInfo(
        Long schoolId,
        String schoolName,
        long totalChallengerCount,
        Map<ChallengerPart, Long> challengerPartCounts
    ) {

        public static SchoolChallengerStatusInfo of(
            Long schoolId,
            String schoolName,
            long totalChallengerCount,
            Map<ChallengerPart, Long> challengerPartCounts
        ) {
            return SchoolChallengerStatusInfo.builder()
                .schoolId(schoolId)
                .schoolName(schoolName)
                .totalChallengerCount(totalChallengerCount)
                .challengerPartCounts(Map.copyOf(challengerPartCounts))
                .build();
        }
    }
}
