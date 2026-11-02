package com.umc.product.test.adapter.in.web.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosResult;
import com.umc.product.test.application.port.in.command.dto.TargetProjectStatus;

public record SeedProjectScenariosResponse(
    List<CreatedProject> createdProjects,
    List<FailedProject> failedProjects
) {

    public record CreatedProject(
        Long projectId,
        TargetProjectStatus finalStatus,
        Long productOwnerMemberId,
        Long chapterId,
        Long schoolId,
        Long applicationFormId,
        List<PartQuota> partQuotas
    ) {

        public static CreatedProject from(SeedProjectScenariosResult.CreatedProject src) {
            return new CreatedProject(
                src.projectId(),
                src.finalStatus(),
                src.productOwnerMemberId(),
                src.chapterId(),
                src.schoolId(),
                src.applicationFormId(),
                src.partQuotas().stream().map(PartQuota::from).toList()
            );
        }
    }

    public record PartQuota(ChallengerPart part, long quota) {

        public static PartQuota from(SeedProjectScenariosResult.PartQuota src) {
            return new PartQuota(src.part(), src.quota());
        }
    }

    public record FailedProject(
        Long projectId,
        TargetProjectStatus reachedStatus,
        TargetProjectStatus intendedStatus,
        String failedStep,
        String reason
    ) {

        public static FailedProject from(SeedProjectScenariosResult.FailedProject src) {
            return new FailedProject(
                src.projectId(),
                src.reachedStatus(),
                src.intendedStatus(),
                src.failedStep(),
                src.reason()
            );
        }
    }

    public static SeedProjectScenariosResponse from(SeedProjectScenariosResult result) {
        return new SeedProjectScenariosResponse(
            result.createdProjects().stream().map(CreatedProject::from).toList(),
            result.failedProjects().stream().map(FailedProject::from).toList()
        );
    }
}
