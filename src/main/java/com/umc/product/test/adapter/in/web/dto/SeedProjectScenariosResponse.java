package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosResult;
import com.umc.product.test.application.port.in.command.dto.TargetProjectStatus;
import java.util.List;

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
        List<PartFill> partFills
    ) {

        public static CreatedProject from(SeedProjectScenariosResult.CreatedProject src) {
            return new CreatedProject(
                src.projectId(),
                src.finalStatus(),
                src.productOwnerMemberId(),
                src.chapterId(),
                src.schoolId(),
                src.applicationFormId(),
                src.partFills() == null ? null
                    : src.partFills().stream().map(PartFill::from).toList()
            );
        }
    }

    public record PartFill(ChallengerPart part, long quota, long filled) {

        public static PartFill from(SeedProjectScenariosResult.PartFill src) {
            return new PartFill(src.part(), src.quota(), src.filled());
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
