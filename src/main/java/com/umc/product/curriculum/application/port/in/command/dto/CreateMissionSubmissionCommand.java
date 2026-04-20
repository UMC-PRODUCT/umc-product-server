package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 미션 제출 커맨드 (챌린저 전용)
 *
 * @param challengerWorkbookId      소속 챌린저 워크북 ID
 * @param originalWorkbookMissionId 제출 대상 원본 워크북 미션 ID
 * @param requesterMemberId         요청 사용자의 멤버 ID (본인 확인용)
 * @param content                   제출 내용 (링크 또는 메모, PLAIN 타입인 경우 null 가능)
 */
@Builder
public record CreateMissionSubmissionCommand(
    @NotNull(message = "챌린저 워크북 ID는 필수입니다")
    Long challengerWorkbookId,

    @NotNull(message = "원본 워크북 미션 ID는 필수입니다")
    Long originalWorkbookMissionId,

    @NotNull(message = "멤버 ID는 필수입니다")
    Long requesterMemberId,

    String content
) {
}