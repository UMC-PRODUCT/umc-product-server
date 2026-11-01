package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;

public record ProjectMatchingCandidateInfo(
    Long memberId, Long challengerId, String name, String nickname, Long schoolId, String schoolName,
    ChallengerPart part
) {
}
