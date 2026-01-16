package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.notification.domain.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmJpaRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByMemberId(Long memberId);

}
