package com.umc.product.notification.application.port.in;

/**
 * FCM 토픽 구독 관리 UseCase
 *
 * 챌린저의 기수/파트/학교/지부 정보를 기반으로
 * 관련 토픽을 자동으로 구독/해제합니다.
 */
public interface ManageFcmTopicUseCase {

    /**
     * 특정 챌린저의 정보를 기반으로 관련 토픽을 구독
     *
     * @param challengerId 챌린저 ID
     */
    void subscribeTopics(Long challengerId);

    /**
     * 특정 챌린저의 정보를 기반으로 관련 토픽을 해제
     *
     * @param challengerId 챌린저 ID
     */
    void unsubscribeTopics(Long challengerId);

    /**
     * 회원의 모든 챌린저에 대해 토픽을 구독 (FCM 토큰 등록 시 사용)
     *
     * @param memberId 회원 ID
     */
    void subscribeAllTopicsByMemberId(Long memberId);

    /**
     * 회원의 모든 챌린저에 대해 토픽을 해제 (FCM 토큰 갱신 시 이전 토큰 정리용)
     *
     * @param memberId 회원 ID
     */
    void unsubscribeAllTopicsByMemberId(Long memberId);
}
