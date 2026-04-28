package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.ReplaceStudyGroupMemberAndMentorCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@Schema(description = "스터디 그룹 멤버 교체 요청")
public record ReplaceStudyGroupMemberAndMentorRequest(
    @Schema(description = "스터디원 목록", example = "[101, 102, 103]")
    @NotNull(message = "변경할 스터디원 목록은 필수입니다.")
    Set<Long> studyMemberIds,

    @Schema(description = "스터디 담당 파트장 ID 목록", example = "[101, 102, 103]")
    @NotNull(message = "변경할 스터디 담당 파트장 ID 목록은 필수입니다")
    Set<Long> studyMentorIds
) {
    public ReplaceStudyGroupMemberAndMentorCommand toCommand(Long groupId) {
        return ReplaceStudyGroupMemberAndMentorCommand.of(groupId, studyMemberIds, studyMentorIds);
    }
}
