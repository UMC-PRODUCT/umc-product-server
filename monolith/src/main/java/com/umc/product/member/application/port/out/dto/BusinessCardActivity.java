package com.umc.product.member.application.port.out.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 명함에 표시할 UMC 활동 이력 한 건입니다.
 * <p>
 * challenger·organization 도메인의 타입을 member 안쪽으로 들이지 않기 위해 member가 소유하는 형태로 옮겨 담습니다.
 *
 * @param generation 기수 번호. 기수 정보를 찾지 못하면 null
 * @param part       파트. 파트가 없는 챌린저는 null
 */
public record BusinessCardActivity(
    Long gisuId,
    Long generation,
    ChallengerPart part
) {
}
