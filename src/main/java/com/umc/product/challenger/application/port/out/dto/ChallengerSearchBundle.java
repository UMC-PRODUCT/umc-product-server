package com.umc.product.challenger.application.port.out.dto;

import java.util.List;
import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerTrack;

/**
 * 챌린저 검색 결과와 파트·트랙별 인원 수를 함께 담는 DTO
 * <p>
 * 검색 조건(BooleanBuilder)을 한 번만 생성하여 검색 쿼리와 파트/트랙 카운트 쿼리에 공유합니다.
 * partCounts는 PART 학습 기수용, trackCounts는 TRACK 학습 기수용 병행 노출입니다.
 */
public record ChallengerSearchBundle(
    List<ChallengerSearchRow> rows,
    Map<ChallengerPart, Long> partCounts,
    Map<ChallengerTrack, Long> trackCounts
) {
}
