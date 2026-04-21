package com.umc.product.curriculum.application.port.in.command.dto.workbook.mission;

import com.umc.product.curriculum.domain.enums.FeedbackResult;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 미션 피드백 작성 커맨드 (운영진 전용)
 *
 * @param missionSubmissionId 피드백 대상 미션 제출물 ID
 * @param reviewerMemberId    피드백 작성자(운영진)의 멤버 ID
 * @param content             피드백 내용
 * @param result              평가 결과 (PASS / FAIL)
 */
@Builder
public record CreateMissionFeedbackCommand(
    @NotNull(message = "미션 제출물 ID는 필수입니다")
    Long missionSubmissionId,

    @NotNull(message = "피드백 작성자 멤버 ID는 필수입니다")
    Long reviewerMemberId,

    String content,

    @NotNull(message = "평가 결과는 필수입니다")
    FeedbackResult result
) {
}
