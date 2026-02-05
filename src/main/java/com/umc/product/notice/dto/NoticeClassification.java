package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import jakarta.validation.constraints.NotNull;

/*
 * 공지 조회 분류 옵션
 *
 * 조회 레벨은 null이 아닌 필드로 결정:
 * - gisuId만: 전체 조회
 * - gisuId + chapterId: 지부별 조회
 * - gisuId + chapterId + schoolId: 학교별 조회
 * - 모두 제공: 파트별 조회
 */
public record NoticeClassification(
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part
) {
}
