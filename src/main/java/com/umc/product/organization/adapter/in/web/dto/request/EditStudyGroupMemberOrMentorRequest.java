package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.AddStudyMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.AddStudyMentorCommand;
import com.umc.product.organization.application.port.in.command.dto.DeleteStudyMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.DeleteStudyMentorCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EditStudyGroupMemberOrMentorRequest(
    @Schema(description = "변경할 대상 회원 ID", example = "100")
    @NotNull(message = "대상 Member ID는 필수입니다.")
    Long memberId
) {
    public AddStudyMentorCommand toAddStudyMentorCommand(Long groupId) {
        return AddStudyMentorCommand.of(groupId, memberId);
    }

    public AddStudyMemberCommand toAddStudyMemberCommand(Long groupId) {
        return AddStudyMemberCommand.of(groupId, memberId);
    }

    public DeleteStudyMentorCommand toDeleteStudyMentorCommand(Long groupId) {
        return DeleteStudyMentorCommand.of(groupId, memberId);
    }

    public DeleteStudyMemberCommand toDeleteStudyMemberCommand(Long groupId) {
        return DeleteStudyMemberCommand.of(groupId, memberId);
    }
}
