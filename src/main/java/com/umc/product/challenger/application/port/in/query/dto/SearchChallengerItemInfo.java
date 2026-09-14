package com.umc.product.challenger.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerTrack;

public record SearchChallengerItemInfo(
        Long challengerId,
        Long memberId,
        Long gisuId,
        Long generation,
        ChallengerPart part,
        List<ChallengerTrack> tracks,
        String name,
        String nickname,
        String schoolName,
        Double pointSum,
        String profileImageLink,
        List<ChallengerRoleType> roleTypes
) {
}
