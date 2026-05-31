package com.umc.product.test.adapter.in.web.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsResult;

public record SeedProjectApplicationsResponse(
    Long matchingRoundId,
    List<ProjectApplications> createdApplications,
    List<FailedApplication> failedApplications,
    Counts counts
) {

    public record ProjectApplications(
        Long projectId,
        List<ApplicationEntry> applications
    ) {

        public static ProjectApplications from(SeedProjectApplicationsResult.ProjectApplications src) {
            return new ProjectApplications(
                src.projectId(),
                src.applications().stream().map(ApplicationEntry::from).toList()
            );
        }
    }

    public record ApplicationEntry(
        Long applicationId,
        Long applicantMemberId,
        ChallengerPart part,
        ProjectApplicationStatus finalStatus
    ) {

        public static ApplicationEntry from(SeedProjectApplicationsResult.ApplicationEntry src) {
            return new ApplicationEntry(
                src.applicationId(),
                src.applicantMemberId(),
                src.part(),
                src.finalStatus()
            );
        }
    }

    public record Counts(
        int submittedTotal,
        int approvedTotal,
        int rejectedTotal,
        int failedTotal
    ) {

        public static Counts from(SeedProjectApplicationsResult.Counts src) {
            return new Counts(
                src.submittedTotal(),
                src.approvedTotal(),
                src.rejectedTotal(),
                src.failedTotal()
            );
        }
    }

    public record FailedApplication(
        Long applicantMemberId,
        Long projectId,
        Long applicationId,
        String failedStep,
        String reason
    ) {

        public static FailedApplication from(SeedProjectApplicationsResult.FailedApplication src) {
            return new FailedApplication(
                src.applicantMemberId(),
                src.projectId(),
                src.applicationId(),
                src.failedStep(),
                src.reason()
            );
        }
    }

    public static SeedProjectApplicationsResponse from(SeedProjectApplicationsResult result) {
        return new SeedProjectApplicationsResponse(
            result.matchingRoundId(),
            result.createdApplications().stream().map(ProjectApplications::from).toList(),
            result.failedApplications().stream().map(FailedApplication::from).toList(),
            Counts.from(result.counts())
        );
    }
}
