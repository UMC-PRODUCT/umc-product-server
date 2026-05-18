package com.umc.product.project.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 멤버 추가 Command (PROJECT-004).
 * <p>
 * 보조 PM 추가 시 {@code part} = {@code PLAN}. 그 외 파트 멤버 추가도 동일 API 로 처리한다 (운영진 관여).
 */
@Builder
public record AddProjectMemberCommand(
    Long projectId,
    Long memberId,
    ChallengerPart part,
    Long requesterMemberId
) {
    public AddProjectMemberCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(part, "part must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
