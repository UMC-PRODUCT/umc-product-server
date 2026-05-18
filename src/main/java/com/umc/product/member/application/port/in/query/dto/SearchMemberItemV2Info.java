package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import java.util.List;

/**
 * /api/v2/member/search 응답의 항목 단위 정보입니다.
 * <p>
 * v1의 SearchMemberItemInfo에 추가로 challengerStatus, isAdminInActiveGisu 두 필드가 포함됩니다.
 */
public record SearchMemberItemV2Info(
    Long memberId,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    Long challengerId,
    Long gisuId,
    Long gisu,
    ChallengerPart part,
    ChallengerStatus challengerStatus,
    List<ChallengerRoleType> roleTypes,
    boolean isAdminInActiveGisu
) {
}
