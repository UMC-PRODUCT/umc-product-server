package com.umc.product.fcm.application.port.out;

import com.umc.product.fcm.entity.FCMToken;
import java.util.Optional;

public interface LoadFcmPort {

    Optional<FCMToken> findByMemberId(Long memberId);

}
