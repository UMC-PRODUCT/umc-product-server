package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRoundInfo;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import java.time.Instant;

public record ProjectMatchingRoundResponse(
    Long id,
    String name,
    String description,
    MatchingType type,
    MatchingPhase phase,
    Long chapterId,
    Instant startsAt,
    Instant endsAt,
    Instant decisionDeadline,
    Instant autoDecisionExecutedAt,
    Long autoDecisionExecutedMemberId,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProjectMatchingRoundResponse from(ProjectMatchingRoundInfo info) {
        return new ProjectMatchingRoundResponse(
            info.id(),
            info.name(),
            info.description(),
            info.type(),
            info.phase(),
            info.chapterId(),
            info.startsAt(),
            info.endsAt(),
            info.decisionDeadline(),
            info.autoDecisionExecutedAt(),
            info.autoDecisionExecutedMemberId(),
            info.createdAt(),
            info.updatedAt()
        );
    }
}
