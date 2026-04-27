package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/*
 * 공지 조회 필터
 *
 * gisuId만: 해당 기수 전체 챌린저 공지 + 조회자 역할에 맞는 운영진 공지
 * gisuId + chapterId: 지부 필터 (챌린저 공지)
 * gisuId + schoolId: 학교 필터 (챌린저 공지)
 * gisuId + part: 파트 필터 (챌린저 공지)
 * 운영진 공지는 조회자의 역할 기반으로 자동 포함됩니다.
 */
@Schema(description = "공지 조회 필터. 챌린저 공지와 운영진 공지를 함께 조회합니다. "
    + "운영진 공지는 조회자의 역할에 따라 자동 포함되며, chapterId/schoolId/part는 챌린저 공지 필터로만 사용됩니다.")
public record NoticeClassification(
    @Schema(description = "기수 ID (필수)", example = "9")
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    @Schema(description = "지부 ID. 챌린저 공지 조회 시에만 사용", example = "3", nullable = true)
    Long chapterId,

    @Schema(description = "학교 ID. 챌린저 공지 조회 시에만 사용", example = "5", nullable = true)
    Long schoolId,

    @Schema(description = "파트. 챌린저 공지 조회 시에만 사용", example = "SPRINGBOOT", nullable = true)
    ChallengerPart part
) {
}
