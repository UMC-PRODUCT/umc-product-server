package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.enums.NoticeTab;
import java.util.Set;

/**
 * 공지를 조회하는 멤버의 소속 컨텍스트.
 * <p>
 * 역할: 인증된 사용자가 "누구인가"를 표현합니다. NoticeViewerInfoAssembler가 auth 컨텍스트로부터 조립하며, 쿼리 레이어에서 챌린저 공지의 파트/지부/학교 필터링과 운영진 공지의 접근
 * 가능 여부 판별에 사용됩니다.
 * <p>
 * - memberParts: 본인 소속 파트 + 파트장으로 담당하는 파트 합산 - viewerRole: 조회자가 가진 최상위 운영진 역할. null이면 운영진 역할 없음. 레벨이 낮을수록 상위 직급
 * (CENTRAL_MEMBER=1 > SCHOOL_CORE=2 > SCHOOL_PART_LEADER=3).
 */
public record NoticeViewerInfo(
    Set<ChallengerPart> memberParts,
    Long schoolId,
    Long chapterId,
    NoticeTab viewerRole
) {
}
