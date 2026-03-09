package com.umc.product.challenger.application.port.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Map;

/**
 * 챌린저 검색 결과와 파트별 인원 수를 함께 담는 DTO
 * <p>
 * 검색 조건(BooleanBuilder)을 한 번만 생성하여 검색 쿼리와 파트별 카운트 쿼리에 공유합니다.
 */
public record ChallengerSearchBundle(
    List<ChallengerSearchRow> rows,
    Map<ChallengerPart, Long> partCounts
) {
}
