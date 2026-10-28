package com.umc.product.project.application.service.policy;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectQuotaMemberPolicy {
    private final ProjectMatchingAdjustmentPolicy adjustmentPolicy;

    /** 공개 명단은 유지하되, 최소 TO에서는 해당 파트의 최종 공개를 기다리는 해제 예약자를 제외한다. */
    public List<ProjectMember> reservedMembers(List<ProjectMember> members,
        List<ProjectMatchingRound> rounds, Instant now) {
        var adjustmentRounds = adjustmentPolicy.findViewableAdjustmentRounds(rounds, now);
        return members.stream()
            .filter(member -> member.getStatus() == ProjectMemberStatus.ACTIVE
                || member.getStatus() == ProjectMemberStatus.PENDING_PUBLICATION)
            .filter(member -> adjustmentRounds.stream().noneMatch(round ->
                Objects.equals(member.getPendingRemovalRoundId(), round.getId())
                    && Objects.equals(member.getProject().getChapterId(), round.getChapterId())
                    && MatchingType.fromPart(member.getPart()).orElse(null) == round.getType()))
            .toList();
    }
}
