package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;

public record SearchMemberItemInfo(
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
    List<ChallengerRoleType> roleTypes
) {
}
