package com.umc.product.challenger.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 챌린저의 파트와 memberId만 들어있는 경량 DTO
 */
public record ChallengerPartInfo(Long memberId, ChallengerPart part) {
}
