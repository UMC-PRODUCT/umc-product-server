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
 * 챌린저 공지의 필터 범위만 지정하며, 운영진 공지는 조회자의 역할(NoticeViewerInfo)을 기반으로 자동 포함됩니다.
 * <p>
 * - gisuId: 필수. 조회할 기수
 * - chapterId / schoolId / part: 챌린저 공지 범위 필터. 조회자 실제 소속으로 자동 보정됩니다(enrichment).
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
    /** 챌린저 공지 유효성 검증용 NoticeTargetInfo 변환 (NoticeTargetPattern.from() 전달 목적) */
    public NoticeTargetInfo toTargetInfo() {
        return new NoticeTargetInfo(
            gisuId, chapterId, schoolId,
            part != null ? List.of(part) : null,
            List.of(NoticeTargetRole.CHALLENGER)
        );
    }
}
