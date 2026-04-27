package com.umc.product.notice.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.enums.NoticeTargetRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 역할: 클라이언트가 "어떤 범위의 공지를 보고 싶은가"를 표현하는 HTTP 쿼리 파라미터.
 * <p>
 * Swagger 문서화, @NotNull 입력 검증, @ParameterObject 바인딩을 위해 NoticeTargetInfo와 분리합니다.
 * <p>
 * - minTargetRole = CHALLENGER: 일반 챌린저 공지 조회. chapterId/schoolId/part 필터 적용 가능.
 * - minTargetRole = CENTRAL_MEMBER/SCHOOL_CORE/SCHOOL_PART_LEADER: 운영진 공지 조회.
 *   요청 역할은 조회자 보유 역할 이하이어야 합니다.
 */
@Schema(description = "공지 조회 필터.")
public record NoticeClassification(
    @Schema(description = "기수 ID (필수)", example = "9")
    @NotNull(message = "기수 ID는 필수입니다")
    Long gisuId,

    @Schema(description = "지부 ID. 챌린저 공지 조회 시에만 사용", example = "3", nullable = true)
    Long chapterId,

    @Schema(description = "학교 ID. 챌린저 공지 조회 시에만 사용 (운영진 공지는 클라이언트가 명시)", example = "5", nullable = true)
    Long schoolId,

    @Schema(description = "파트. 챌린저 공지 조회 시에만 사용", example = "SPRINGBOOT", nullable = true)
    ChallengerPart part,

    @Schema(description = "대상 역할 하한선. CHALLENGER면 일반 공지, 그 외 값이면 운영진 공지.",
        example = "CHALLENGER")
    @NotNull(message = "대상 역할은 필수입니다.")
    NoticeTargetRole minTargetRole
) {
    public boolean isChallengerQuery() {
        return minTargetRole == NoticeTargetRole.CHALLENGER;
    }

    /** 챌린저 공지 유효성 검증용 NoticeTargetInfo 변환 (NoticeTargetPattern.from() 전달 목적) */
    public NoticeTargetInfo toTargetInfo() {
        return new NoticeTargetInfo(
            gisuId, null, null,
            part != null ? List.of(part) : null,
            NoticeTargetRole.CHALLENGER
        );
    }
}
