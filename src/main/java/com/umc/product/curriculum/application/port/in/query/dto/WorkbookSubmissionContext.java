package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 워크북 제출 현황 조회 컨텍스트
 *
 * @param schoolId 조회 대상 학교 ID
 * @param part     조회 대상 파트 (null이면 모든 파트)
 */
public record WorkbookSubmissionContext(
        Long schoolId,
        ChallengerPart part
) {
}
