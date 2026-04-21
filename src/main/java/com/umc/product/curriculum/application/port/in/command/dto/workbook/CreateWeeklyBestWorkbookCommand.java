package com.umc.product.curriculum.application.port.in.command.dto.workbook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 베스트 워크북 선정 커맨드 (운영진 전용)
 *
 * @param decidedMemberId    선정자(운영진)의 멤버 ID
 * @param bestMemberId       베스트 워크북으로 선정받을 챌린저의 멤버 ID
 * @param weeklyCurriculumId 해당 주차의 주차별 커리큘럼 ID
 * @param studyGroupId       스터디 그룹 ID
 * @param reason             선정 사유
 */
@Builder
public record CreateWeeklyBestWorkbookCommand(
    @NotNull(message = "선정자 멤버 ID는 필수입니다")
    Long decidedMemberId,

    @NotNull(message = "선정 대상 멤버 ID는 필수입니다")
    Long bestMemberId,

    @NotNull(message = "주차별 커리큘럼 ID는 필수입니다")
    Long weeklyCurriculumId,

    @NotNull(message = "스터디 그룹 ID는 필수입니다")
    Long studyGroupId,

    @NotBlank(message = "선정 이유는 필수입니다.")
    String reason
) {
}
