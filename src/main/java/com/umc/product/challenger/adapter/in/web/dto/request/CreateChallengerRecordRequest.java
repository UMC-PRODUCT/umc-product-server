package com.umc.product.challenger.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;

public record CreateChallengerRecordRequest(
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part,
    String memberName,
    ChallengerRoleType challengerRoleType
) {
}
