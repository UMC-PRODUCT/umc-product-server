package com.umc.product.curriculum.application.port.in.command.dto.workbook.mission;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 미션 피드백 삭제 커맨드 (운영진 전용)
 * <p>
 * 해당 기수 종료 이후에는 삭제가 불가능합니다.
 *
 * @param missionFeedbackId 삭제 대상 피드백 ID
 * @param operatorMemberId  요청 운영진의 멤버 ID (본인 피드백 여부 검증 및 감사 추적용)
 */
@Builder
public record DeleteMissionFeedbackCommand(
    @NotNull(message = "피드백 ID는 필수입니다")
    Long missionFeedbackId,

    @NotNull(message = "요청자 멤버 ID는 필수입니다")
    Long operatorMemberId
) {
}
