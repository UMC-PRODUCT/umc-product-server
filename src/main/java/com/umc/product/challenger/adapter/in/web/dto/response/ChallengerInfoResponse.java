package com.umc.product.challenger.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import java.util.List;

public record ChallengerInfoResponse(
        Long challengerId,
        Long memberId,
        Long gisu,
        ChallengerPart part,
        List<ChallengerPointInfo> challengerPoints,

        // 멤버 정보
        String name,
        String nickname,
        String email,
        Long schoolId,
        String schoolName,
        String profileImageLink,
        MemberStatus status
) {
}
