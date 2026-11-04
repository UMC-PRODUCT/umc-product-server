package com.umc.product.test.domain.roster;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * 봇이 지원하지 않고 QA가 직접 지원하는 몫. 지부·매칭 파트마다 A·B·C 세 명이다.
 */
public record QaReservedApplicant(RosterKey key, ChallengerPart part, char label) {
}
