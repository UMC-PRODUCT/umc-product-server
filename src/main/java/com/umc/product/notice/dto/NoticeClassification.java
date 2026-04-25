package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.enums.NoticeTab;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/*
 * 공지 조회 분류 옵션
 *
 * tab = CHALLENGER (기본값): 일반 챌린저 공지 조회
 *   - gisuId만: 전체 조회
 *   - gisuId + chapterId: 지부 필터
 *   - gisuId + schoolId: 학교 필터
 *   - gisuId + part: 파트 필터
 *
 * tab = CENTRAL_STAFF: 중앙운영진 공지 조회 (조회자의 중앙 역할 기반 자동 필터링, 위 필터 무시)
 * tab = SCHOOL_STAFF: 교내운영진 공지 조회 (조회자의 교내 역할 + 소속 학교 기반 자동 필터링, 위 필터 무시)
 */
@Schema(description = "공지 조회 필터. tab=CHALLENGER(기본): 일반 공지, CENTRAL_STAFF: 중앙운영진 공지, SCHOOL_STAFF: 교내운영진 공지")
public record NoticeClassification(
    @Schema(description = "기수 ID (필수)", example = "9")
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    @Schema(description = "지부 ID. 일반 공지 조회 시에만 사용", example = "3", nullable = true)
    Long chapterId,

    @Schema(description = "학교 ID. 일반 공지 조회 시에만 사용", example = "5", nullable = true)
    Long schoolId,

    @Schema(description = "파트. 일반 공지 조회 시에만 사용", example = "SPRINGBOOT", nullable = true)
    ChallengerPart part,

    @Schema(description = "조회할 공지 탭. CHALLENGER: 일반 공지, CENTRAL_STAFF: 중앙운영진 공지, SCHOOL_STAFF: 교내운영진 공지",
        example = "CHALLENGER")
    @NotNull(message = "탭 구분은 필수입니다")
    NoticeTab tab
) {
}
