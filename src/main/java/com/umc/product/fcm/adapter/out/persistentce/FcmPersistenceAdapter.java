package com.umc.product.fcm.adapter.out.persistentce;

import com.umc.product.fcm.application.port.out.LoadFcmPort;
import com.umc.product.fcm.application.port.out.SaveFcmPort;
import com.umc.product.fcm.entity.FcmToken;
import com.umc.product.fcm.entity.exception.FcmErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
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
    public void save(FcmToken fcmToken) {
        fcmJpaRepository.save(fcmToken);
    }
}
