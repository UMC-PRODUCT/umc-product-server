package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import jakarta.validation.constraints.NotNull;

/**
 * DRAFT 상태의 프로젝트 생성 요청 (PROJECT-101).
 */
public record CreateDraftProjectRequest(
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId
) {
    public CreateDraftProjectCommand toCommand(Long requesterMemberId) {
        return CreateDraftProjectCommand.builder()
            .gisuId(gisuId)
            .productOwnerMemberId(requesterMemberId)
            .build();
    }
}
