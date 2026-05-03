package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import jakarta.validation.constraints.NotNull;

/**
 * DRAFT 상태의 프로젝트 생성 요청 (PROJECT-101).
 * <p>
 * {@code productOwnerMemberId} 미지정 시 호출자 본인이 PO 로 등록된다 (PM 본인 동선).
 * 운영진(회장/지부장/총괄단)이 다른 PLAN 챌린저를 PO 로 지정하려면 명시한다.
 */
public record CreateDraftProjectRequest(
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    Long productOwnerMemberId
) {
    public CreateDraftProjectCommand toCommand(Long requesterMemberId) {
        Long targetPo = productOwnerMemberId == null ? requesterMemberId : productOwnerMemberId;
        return CreateDraftProjectCommand.builder()
            .gisuId(gisuId)
            .productOwnerMemberId(targetPo)
            .requesterMemberId(requesterMemberId)
            .build();
    }
}
