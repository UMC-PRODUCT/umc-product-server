package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import jakarta.validation.constraints.NotNull;

/**
 * PROJECT-004 요청. 보조 PM 추가는 {@code part = PLAN} 으로 전달.
 */
public record AddProjectMemberRequest(
    @NotNull(message = "memberId 는 필수입니다")
    Long memberId,

    @NotNull(message = "part 는 필수입니다")
    ChallengerPart part
) {
    public AddProjectMemberCommand toCommand(Long projectId, Long requesterMemberId) {
        return AddProjectMemberCommand.builder()
            .projectId(projectId)
            .memberId(memberId)
            .part(part)
            .requesterMemberId(requesterMemberId)
            .build();
    }
}
