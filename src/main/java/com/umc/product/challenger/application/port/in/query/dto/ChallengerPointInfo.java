package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.challenger.domain.enums.PointType;

/**
 * 챌린저에게 부여된 상벌점에 대한 정보를 나타냅니다.
 * <p>
 * 점수는 유형별로 고정되어 있습니다. 총점은 따로 계산하지 않으며 FE단에서 처리하여야 합니다.
 *
 * @param id          상벌점 ID
 * @param pointType   상벌점 유형
 * @param description 상벌점에 대한 설명
 */
public record ChallengerPointInfo(
        Long id,
        PointType pointType,
        Double point,
        String description
) {
}
