package com.umc.product.analytics.adapter.in.web.dto.response;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsStudyGroupsInfo;

import lombok.Builder;

@Builder
public record AdminOperationsStudyGroupsResponse(
    long studyGroupCount,
    long studyGroupScheduleCount
) {

    public static AdminOperationsStudyGroupsResponse from(AdminOperationsStudyGroupsInfo info) {
        return AdminOperationsStudyGroupsResponse.builder()
            .studyGroupCount(info.studyGroupCount())
            .studyGroupScheduleCount(info.studyGroupScheduleCount())
            .build();
    }
}
