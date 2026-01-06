package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.enums.ChallengerPart;
import lombok.Builder;

@Builder
public record ChallengerPublicInfo(
        Long challengerId,
        Long memberId,
        Long gisuId,
        ChallengerPart part
) {

    public static ChallengerPublicInfo from(Challenger challenger) {
        return ChallengerPublicInfo.builder()
                .challengerId(challenger.getId())
                .memberId(challenger.getMemberId())
                .gisuId(challenger.getGisuId())
                .part(challenger.getPart())
                .build();

    }
}
