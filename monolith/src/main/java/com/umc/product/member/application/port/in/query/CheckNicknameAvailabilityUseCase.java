package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.NicknameAvailabilityInfo;

public interface CheckNicknameAvailabilityUseCase {

    NicknameAvailabilityInfo check(Long schoolId, String nickname);
}
