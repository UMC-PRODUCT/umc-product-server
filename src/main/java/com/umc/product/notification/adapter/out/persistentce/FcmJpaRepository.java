package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.notification.domain.FcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmJpaRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByMemberIdAndFcmToken(Long memberId, String fcmToken);

    List<FcmToken> findAllByMemberIdAndIsActiveTrue(Long memberId);

    List<FcmToken> findAllByMemberIdInAndIsActiveTrue(List<Long> memberIds);

    /** @deprecated 토픽 구독 용도. 토큰 기반 전환 후 제거 예정 */
    @Deprecated(since = "token-based migration", forRemoval = true)
    Optional<FcmToken> findByMemberId(Long memberId);

}
