package com.umc.product.curriculum.application.port.in.command.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 미션 피드백 수정 커맨드 (운영진 전용)
 * <p>
 * PASS → FAIL 변경은 불가능합니다.
 *
 * @param missionFeedbackId 수정 대상 피드백 ID
 * @param reviewerMemberId  요청 운영진의 멤버 ID (본인 피드백 여부 검증용)
 * @param content           변경할 피드백 내용
 */
@Builder
public record EditMissionFeedbackCommand(
    @NotNull(message = "피드백 ID는 필수입니다")
    Long missionFeedbackId,

    @NotNull(message = "요청자 멤버 ID는 필수입니다")
    Long reviewerMemberId,

    String content
) {
}
