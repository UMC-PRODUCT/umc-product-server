package com.umc.product.project.application.service.policy;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionCapabilityInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@Component
public class ProjectQuotaEditPolicy {
    public EditWindow evaluate(ChallengerPart part, List<ProjectMatchingRound> rounds, Instant now) {
        Instant start = rounds.stream().map(ProjectMatchingRound::getStartsAt).min(Instant::compareTo).orElse(null);
        if (start == null || now.isBefore(start)) {
            return new EditWindow(ProjectPermissionCapabilityInfo.allow(), null);
        }
        return new EditWindow(
            ProjectPermissionCapabilityInfo.denied(ProjectPermissionReason.PART_QUOTA_MATCHING_IN_PROGRESS), null);
    }

    public void validate(ChallengerPart part, List<ProjectMatchingRound> rounds, Instant now) {
        EditWindow window = evaluate(part, rounds, now);
        if (!window.capability().allowed()) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_PART_QUOTA_MATCHING_LOCKED,
                part.getDisplayName() + ": " + window.capability().reason());
        }
    }

    public record EditWindow(ProjectPermissionCapabilityInfo capability, Instant editableAfter) {
    }
}
