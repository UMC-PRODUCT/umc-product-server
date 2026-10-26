package com.umc.product.project.application.service.policy;

import org.springframework.stereotype.Component;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.service.query.ProjectQuotaManagementDataLoader;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectQuotaCapacityPolicy {
    private final ProjectQuotaManagementDataLoader dataLoader;

    public void validate(Long gisuId, Long chapterId) {
        try {
            validateSnapshot(gisuId, chapterId);
        } catch (ArithmeticException exception) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_CAPACITY_EXCEEDED);
        }
    }

    private void validateSnapshot(Long gisuId, Long chapterId) {
        var snapshot = dataLoader.load(gisuId, chapterId);
        var allocated = snapshot.allocated(snapshot.projects());
        for (ChallengerPart part : ChallengerPart.values()) {
            if (part == ChallengerPart.ADMIN) {
                continue;
            }
            if (allocated.getOrDefault(part, 0L) > snapshot.population().getOrDefault(part, 0L)) {
                throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_CAPACITY_EXCEEDED,
                    part.getDisplayName() + " TO 합계가 해당 지부의 파트 인원을 초과했어요.");
            }
        }
    }
}
