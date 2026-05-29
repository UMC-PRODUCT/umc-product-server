package com.umc.product.test.adapter.in.web.dto;

import java.util.List;

import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsResult;

public record SeedProjectApplicationsResponse(
    int submittedCount,
    int approvedCount,
    int rejectedCount,
    int failedCount,
    List<FailedApplication> failures
) {

    public record FailedApplication(
        Long applicantMemberId,
        Long projectId,
        String failedStep,
        String reason
    ) {

        public static FailedApplication from(SeedProjectApplicationsResult.FailedApplication src) {
            return new FailedApplication(
                src.applicantMemberId(),
                src.projectId(),
                src.failedStep(),
                src.reason()
            );
        }
    }

    public static SeedProjectApplicationsResponse from(SeedProjectApplicationsResult result) {
        return new SeedProjectApplicationsResponse(
            result.submittedCount(),
            result.approvedCount(),
            result.rejectedCount(),
            result.failures().size(),
            result.failures().stream().map(FailedApplication::from).toList()
        );
    }
}
