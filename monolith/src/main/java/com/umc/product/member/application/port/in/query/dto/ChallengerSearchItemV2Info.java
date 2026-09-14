package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.List;

/**
 * /api/v2/challenger/search 응답의 항목 단위 정보입니다.
 * <p>
 * 챌린저 1개당 1개 항목으로, 같은 회원이 여러 기수 챌린저 이력을 가지면 여러 row로 분리되어 반환됩니다.
 * 검색의 주체가 "챌린저"라는 의미에 부합하는 응답 형태입니다.
 */
public record ChallengerSearchItemV2Info(
    Long memberId,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    Long challengerId,
    Long gisuId,
    Long generation,
    ChallengerPart part,
    ChallengerStatus challengerStatus,
    List<ChallengerRoleType> roleTypes,
    boolean isAdminInActiveGisu
) {
}
