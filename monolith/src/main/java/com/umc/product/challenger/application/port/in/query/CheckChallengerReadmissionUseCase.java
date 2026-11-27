package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerReadmissionInfo;

public interface CheckChallengerReadmissionUseCase {

    ChallengerReadmissionInfo checkReadmission(Long requesterMemberId, String email);
}
