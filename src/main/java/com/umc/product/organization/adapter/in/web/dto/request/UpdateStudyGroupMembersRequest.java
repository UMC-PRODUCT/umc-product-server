package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupMembersCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "스터디 그룹 멤버 수정 요청")
public record UpdateStudyGroupMembersRequest(
        @Schema(description = "스터디원 챌린저 ID 목록", example = "[101, 102, 103]")
        @NotNull(message = "챌린저 ID 목록은 필수입니다")
        Set<Long> challengerIds
) {
    public UpdateStudyGroupMembersCommand toCommand(Long groupId) {
        return new UpdateStudyGroupMembersCommand(groupId, challengerIds);
    }
}
