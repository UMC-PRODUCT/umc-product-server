package com.umc.product.notification.application.port.out;

import com.umc.product.notification.domain.FcmToken;
import java.util.Optional;

public interface LoadFcmPort {

    FcmToken findByMemberId(Long memberId);

    Optional<FcmToken> findOptionalByMemberId(Long memberId);

}
