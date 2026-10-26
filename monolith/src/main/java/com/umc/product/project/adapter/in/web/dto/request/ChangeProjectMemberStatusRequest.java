package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.ChangeProjectMemberStatusCommand;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * PROJECT-006 요청 (팀원 활동 완료·중도 이탈·퇴출).
 * <p>
 * 변경할 {@code status} 와 사유를 함께 전달한다. {@code reason} 은 팀원의 최근 상태 변경 사유로 저장하며 필수다.
 */
public record ChangeProjectMemberStatusRequest(
    @Schema(allowableValues = {"COMPLETED", "WITHDRAWN", "DISMISSED"})
    @NotNull(message = "status 는 필수입니다") ProjectMemberStatus status,

    @NotBlank(message = "reason 은 필수입니다") @Size(max = 255) String reason
) {
    public ChangeProjectMemberStatusCommand toCommand(Long projectId, Long memberId, Long requesterMemberId) {
        return ChangeProjectMemberStatusCommand.builder()
            .projectId(projectId)
            .memberId(memberId)
            .status(status)
            .reason(reason)
            .requesterMemberId(requesterMemberId)
            .build();
    }
}
