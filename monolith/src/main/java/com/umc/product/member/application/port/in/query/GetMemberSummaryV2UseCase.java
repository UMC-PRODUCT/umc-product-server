package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.MemberSummaryV2Info;

/**
 * /api/v2/member/me BFF UseCase.
 * <p>
 * 활성 기수 멤버십, 챌린저 이력, 활동일 합산, 기수별 상벌점 등을 한 응답으로 제공합니다.
 */
public interface GetMemberSummaryV2UseCase {
    MemberSummaryV2Info getSummaryByMemberId(Long memberId);
}
