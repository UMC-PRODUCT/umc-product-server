package com.umc.product.fcm.adapter.out.persistentce;

import com.umc.product.fcm.entity.FCMToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmJpaRepository extends JpaRepository<FCMToken, Long> {

    Optional<FCMToken> findByMemberId(Long memberId);

}
