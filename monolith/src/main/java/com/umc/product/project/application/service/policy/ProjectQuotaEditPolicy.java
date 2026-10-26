package com.umc.product.project.application.service.policy;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionCapabilityInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@Component
public class ProjectQuotaEditPolicy {
    public EditWindow evaluate(ChallengerPart part, List<ProjectMatchingRound> rounds, Instant now) {
        var type = MatchingType.fromPart(part);
        if (part == ChallengerPart.PLAN) {
            List<EditWindow> windows = List.of(evaluate(ChallengerPart.DESIGN, rounds, now),
                evaluate(ChallengerPart.WEB_PRODUCT_ENGINEER, rounds, now));
            var blocked = windows.stream().filter(window -> !window.capability().allowed()).toList();
            if (!blocked.isEmpty()) {
                Instant deadline = blocked.stream().anyMatch(window -> window.editableAfter() == null) ? null
                    : blocked.stream().map(EditWindow::editableAfter).max(Instant::compareTo).orElse(null);
                var reason = blocked.stream().filter(window -> window.editableAfter() == null).findFirst()
                    .orElse(blocked.getFirst()).capability();
                return new EditWindow(reason, deadline);
            }
        }
        if (type.isEmpty()) {
            return new EditWindow(ProjectPermissionCapabilityInfo.allow(), null);
        }
        List<ProjectMatchingRound> relevant = rounds.stream()
            .filter(round -> round.getType() == type.get() && !round.isRandomMatching()).toList();
        Instant start = relevant.stream().map(ProjectMatchingRound::getStartsAt).min(Instant::compareTo).orElse(null);
        Instant deadline = relevant.stream().filter(round -> round.getPhase() == MatchingPhase.THIRD)
            .map(ProjectMatchingRound::getDecisionDeadline).max(Instant::compareTo).orElse(null);
        if (start == null || now.isBefore(start) || (deadline != null && now.isAfter(deadline))) {
            return new EditWindow(ProjectPermissionCapabilityInfo.allow(), null);
        }
        var reason = deadline == null ? ProjectPermissionReason.PART_QUOTA_MATCHING_SCHEDULE_INCOMPLETE
            : ProjectPermissionReason.PART_QUOTA_MATCHING_IN_PROGRESS;
        return new EditWindow(ProjectPermissionCapabilityInfo.denied(reason), deadline);
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
