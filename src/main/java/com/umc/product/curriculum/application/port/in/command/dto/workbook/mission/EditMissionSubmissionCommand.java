package com.umc.product.curriculum.application.port.in.command.dto.workbook.mission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 미션 제출물 수정 커맨드 (챌린저 전용)
 *
 * @param missionSubmissionId 수정 대상 미션 제출물 ID
 * @param requesterMemberId   요청 사용자의 멤버 ID (본인 확인용)
 * @param content             변경할 제출 내용
 */
@Builder
public record EditMissionSubmissionCommand(
    @NotNull(message = "미션 제출물 ID는 필수입니다")
    Long missionSubmissionId,

    @NotNull(message = "멤버 ID는 필수입니다")
    Long requesterMemberId,

    @NotBlank(message = "수정할 내용은 필수입니다.")
    String content
) {
}
