package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.CreateExtensionCommand;
import java.util.List;

public record CreateExtensionRequest(
    String recruitmentName,
    List<ChallengerPart> parts
) {
    // 임시 저장 초기 진입 시 빈 객체 생성을 위한 메서드 유지
    public static CreateExtensionRequest empty() {
        return new CreateExtensionRequest(null, null);
    }

    public CreateExtensionCommand toCommand(Long baseId, Long memberId) {
        return new CreateExtensionCommand(
            memberId,
            baseId,
            this.recruitmentName,
            this.parts
        );
    }
}
