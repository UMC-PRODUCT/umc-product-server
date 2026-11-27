package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationDetailInfo;

public interface GetChallengerDeactivationDetailUseCase {

    ChallengerDeactivationDetailInfo getDetail(Long requesterMemberId, Long deactivationId);
}
