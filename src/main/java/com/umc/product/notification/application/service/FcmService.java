package com.umc.product.notification.application.service;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService implements ManageFcmUseCase {

    private final LoadFcmPort loadFcmPort;
    private final SaveFcmPort saveFcmPort;

    @Override
    @Transactional
    public void registerFcmToken(Long memberId, FcmRegistrationRequest request) {
        loadFcmPort.findByMemberIdAndToken(memberId, request.fcmToken())
            .ifPresentOrElse(
                FcmToken::activate,
                () -> saveFcmPort.save(FcmToken.createFCMToken(memberId, request.fcmToken()))
            );
    }

}
