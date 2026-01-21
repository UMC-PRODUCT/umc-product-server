package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Schema(description = "스터디 그룹 생성 요청")
public record CreateStudyGroupRequest(
        @Schema(description = "그룹명", example = "React A팀", minLength = 1, maxLength = 50)
        @NotBlank(message = "그룹명은 필수입니다")
        @Size(min = 1, max = 50, message = "그룹명은 1~50자")
        String name,

        @Schema(description = "파트", example = "WEB")
        @NotNull(message = "파트는 필수입니다")
        ChallengerPart part,

        @Schema(description = "파트장 챌린저 ID", example = "101")
        @NotNull(message = "파트장 ID는 필수입니다")
        @Positive(message = "파트장 ID는 양수여야 합니다")
        Long leaderId,

        @Schema(description = "스터디원 챌린저 ID 목록", example = "[102, 103, 104]")
        Set<Long> memberIds
) {
    public CreateStudyGroupCommand toCommand() {
        return new CreateStudyGroupCommand(name, part, leaderId, memberIds != null ? memberIds : new HashSet<>());
    }
}
