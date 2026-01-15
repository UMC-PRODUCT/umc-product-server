package com.umc.product.fcm.adapter.out.persistentce;

import com.umc.product.fcm.entity.FcmToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmJpaRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByMemberId(Long memberId);

}
