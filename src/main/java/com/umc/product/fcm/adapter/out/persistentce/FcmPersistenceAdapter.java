package com.umc.product.fcm.adapter.out.persistentce;

import com.umc.product.fcm.application.port.out.LoadFcmPort;
import com.umc.product.fcm.application.port.out.SaveFcmPort;
import com.umc.product.fcm.entity.FCMToken;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmPersistenceAdapter implements LoadFcmPort, SaveFcmPort {

    private final FcmJpaRepository fcmJpaRepository;

    public Optional<FCMToken> findByMemberId(Long memberId) {
        return fcmJpaRepository.findByMemberId(memberId);
    }

    public void save(FCMToken fcmToken) {
        fcmJpaRepository.save(fcmToken);
    }
}
