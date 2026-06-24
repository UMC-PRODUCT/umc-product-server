package com.umc.product.notification.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Deprecated(since = "token-based migration", forRemoval = true)
public class FcmTopicService implements ManageFcmTopicUseCase {

    @Override
    @Deprecated(since = "token-based migration", forRemoval = true)
    public void subscribeAllTopicsByMemberId(Long memberId) {
        log.debug("[DEPRECATED] 토픽 기반 구독을 사용하지 않습니다: memberId={}", memberId);
    }

    @Override
    @Deprecated(since = "token-based migration", forRemoval = true)
    public void unsubscribeAllTopicsByMemberId(Long memberId) {
        log.debug("[DEPRECATED] 토픽 기반 구독 해제를 사용하지 않습니다: memberId={}", memberId);
    }

    @Override
    @Deprecated(since = "token-based migration", forRemoval = true)
    public void unsubscribeTokenFromTopics(String fcmToken, Long memberId) {
        log.debug("[DEPRECATED] 토픽 기반 토큰 구독 해제를 사용하지 않습니다: memberId={}", memberId);
    }

    @Override
    @Deprecated(since = "token-based migration", forRemoval = true)
    public void unsubscribeLegacyTopics(Long memberId) {
        log.debug("[DEPRECATED] 레거시 토픽 해제를 사용하지 않습니다: memberId={}", memberId);
    }

    @Override
    @Deprecated(since = "token-based migration", forRemoval = true)
    public void subscribeToTopic(List<String> fcmTokens, String topic) {
        log.debug("[DEPRECATED] 토픽 구독을 사용하지 않습니다: topic={}", topic);
    }

    @Override
    @Deprecated(since = "token-based migration", forRemoval = true)
    public void unsubscribeFromTopic(List<String> fcmTokens, String topic) {
        log.debug("[DEPRECATED] 토픽 구독 해제를 사용하지 않습니다: topic={}", topic);
    }

    @Override
    @Deprecated(since = "token-based migration", forRemoval = true)
    public void resubscribeAllLegacyTopics() {
        log.debug("[DEPRECATED] 전체 레거시 토픽 재구독을 사용하지 않습니다.");
    }
}
