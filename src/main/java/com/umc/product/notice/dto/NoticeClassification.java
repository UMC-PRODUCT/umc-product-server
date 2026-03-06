package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "공지 조회 필터. 조회 범위는 입력한 필드 조합으로 결정됩니다. "
    + "전체필터 : gisuId만 입력, 지부필터 : gisuId + chapterId, 학교필터 : gisuId + schoolId, 파트필터 : gisuId + part")
public record NoticeClassification(
    @Schema(description = "기수 ID (필수). 이 기수에 해당하는 공지만 조회", example = "9")
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    @Schema(description = "지부 ID",
        example = "3", nullable = true)
    Long chapterId,

    @Schema(description = "학교 ID",
        example = "5", nullable = true)
    Long schoolId,

    @Schema(description = "파트. null이면 파트 구분 없이 조회. 값을 넣으면 해당 파트 공지만 필터링",
        example = "SPRINGBOOT", nullable = true)
    ChallengerPart part
) {
}
