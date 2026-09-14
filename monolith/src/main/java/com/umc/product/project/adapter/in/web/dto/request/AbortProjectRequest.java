package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.AbortProjectCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 프로젝트 중단(abort) 요청 DTO. 사유는 필수이며 운영진 감사 로그용으로 사용됩니다.
 */
public record AbortProjectRequest(
    @NotBlank
    @Size(max = 255)
    String reason
) {
    public AbortProjectCommand toCommand(Long projectId, Long requesterMemberId) {
        return AbortProjectCommand.builder()
            .projectId(projectId)
            .requesterMemberId(requesterMemberId)
            .reason(reason)
            .build();
    }
}
