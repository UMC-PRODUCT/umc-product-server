package com.umc.product.challenger.application.port.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import lombok.Builder;

/**
 * 챌린저 검색 쿼리의 단일 행 결과를 담는 DTO
 * <p>
 * 메인 검색 쿼리에서 challenger + member + school을 JOIN하여 한 번에 가져온 결과입니다.
 */
@Builder
public record ChallengerSearchRow(
    Long challengerId,
    Long memberId,
    Long gisuId,
    ChallengerPart part,
    ChallengerStatus status,
    String memberName,
    String memberNickname,
    String schoolName,
    String profileImageId
) {
}
