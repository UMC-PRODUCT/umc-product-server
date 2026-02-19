package com.umc.product.notification.application.service;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.RefreshFcmTokenUseCase;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.exception.FcmDomainException;
import com.umc.product.notification.domain.exception.FcmErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmRegistrationService implements RefreshFcmTokenUseCase {

    private final ManageFcmUseCase manageFcmUseCase;
    private final ManageFcmTopicUseCase manageFcmTopicUseCase;
    private final LoadFcmPort loadFcmPort;

    @Override
    @Transactional
    public void refreshTokenAndSubscriptions(Long userId, FcmRegistrationRequest request) {
        // 1. 이전 토큰으로 토픽 구독 해제 (실패해도 계속 진행)
        String oldTokenValue = loadFcmPort.findOptionalByMemberId(userId)
            .map(FcmToken::getFcmToken)
            .orElse(null);

        try {
            manageFcmTopicUseCase.unsubscribeAllTopicsByMemberId(userId);
        } catch (Exception e) {
            log.warn("토큰 갱신 중 이전 토픽 구독 해제 실패 userId={}, 계속 진행합니다.", userId, e);
        }

        // 2. 토큰 등록/업데이트
        manageFcmUseCase.registerFcmToken(userId, request);

        // 3. 새 토큰으로 토픽 재구독
        try {
            manageFcmTopicUseCase.subscribeAllTopicsByMemberId(userId);
        } catch (Exception e) {
            log.error("토큰 갱신 후 토픽 재구독 실패 userId={}, 보상 로직을 실행합니다.", userId, e);
            compensateTokenAndSubscriptions(userId, oldTokenValue);
            throw new FcmDomainException(FcmErrorCode.TOPIC_SUBSCRIBE_FAILED);
        }
    }

    private void compensateTokenAndSubscriptions(Long userId, String oldTokenValue) {
        try {
            if (oldTokenValue != null) {
                FcmToken token = loadFcmPort.findByMemberId(userId);
                if (token != null) {
                    token.updateToken(oldTokenValue);
                }
                manageFcmTopicUseCase.subscribeAllTopicsByMemberId(userId);
                log.info("보상 로직 완료: 이전 토큰으로 복구 및 재구독 userId={}", userId);
            }
        } catch (Exception compensationEx) {
            log.error("보상 로직도 실패 userId={}, 수동 확인이 필요합니다.", userId, compensationEx);
        }
    }
}
