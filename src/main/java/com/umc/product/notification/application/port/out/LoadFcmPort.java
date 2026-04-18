package com.umc.product.notification.application.port.out;

import com.umc.product.notification.domain.FcmToken;
import java.util.List;
import java.util.Optional;

public interface LoadFcmPort {

    Optional<FcmToken> findByMemberIdAndToken(Long memberId, String fcmToken);

    List<FcmToken> findAllActiveByMemberId(Long memberId);

    List<FcmToken> findAllActiveByMemberIds(List<Long> memberIds);

    /** @deprecated 토픽 구독 용도로만 사용되던 메서드. 토큰 기반 전환 후 제거 예정 */
    @Deprecated(since = "token-based migration", forRemoval = true)
    Optional<FcmToken> findOptionalByMemberId(Long memberId);

}
