package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.exception.FcmErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmPersistenceAdapter implements LoadFcmPort, SaveFcmPort {

    private final FcmJpaRepository fcmJpaRepository;

    @Override
    public FcmToken findByMemberId(Long memberId) {
        return fcmJpaRepository.findByMemberId(memberId).orElseThrow(() -> new BusinessException(Domain.FCM,
                FcmErrorCode.USER_FCM_NOT_FOUND));
    }

    @Override
    public Optional<FcmToken> findOptionalByMemberId(Long memberId) {
        return fcmJpaRepository.findByMemberId(memberId);
    }

    @Override
    public void save(FcmToken fcmToken) {
        fcmJpaRepository.save(fcmToken);
    }
}
