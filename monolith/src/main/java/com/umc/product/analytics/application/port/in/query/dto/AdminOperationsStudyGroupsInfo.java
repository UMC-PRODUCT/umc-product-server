package com.umc.product.analytics.application.port.in.query.dto;

import lombok.Builder;

@Builder
public record AdminOperationsStudyGroupsInfo(
    long studyGroupCount,
    long studyGroupScheduleCount
) {

    public static AdminOperationsStudyGroupsInfo of(long studyGroupCount, long studyGroupScheduleCount) {
        return AdminOperationsStudyGroupsInfo.builder()
            .studyGroupCount(studyGroupCount)
            .studyGroupScheduleCount(studyGroupScheduleCount)
            .build();
    }
}
