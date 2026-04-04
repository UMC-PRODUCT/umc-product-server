package com.umc.product.notification.application.port.in;

import java.util.List;

/**
 * FCM 토픽 구독 관리 UseCase
 *
 * <pre>
 * [토픽 구조]
 * - member 토픽  : {prefix}-member-{memberId}  → 개인 알림용, 멤버 단위로 관리
 * - org 토픽     : 기수/파트/학교/지부 조합       → 공지 발송용, 챌린저 단위로 관리
 *
 * [구독 흐름]
 * FCM 토큰 등록/갱신 → subscribeAllTopicsByMemberId
 *   → member 토픽 1회 구독
 *   → 모든 챌린저의 org 토픽 구독 (fcm_token_topic에 이미 있으면 스킵)
 *
 * 새 챌린저 등록 시에도 subscribeAllTopicsByMemberId 호출 가능
 *   → 기존 구독 토픽은 DB 체크로 스킵, 새 챌린저 토픽만 추가됨
 *
 * [해제 흐름]
 * FCM 토큰 갱신 → unsubscribeTokenFromTopics (이전 토큰 정리)
 *   → fcm_token_topic DB 조회 기반으로 해제 후 레코드 삭제
 * </pre>
 */
public interface ManageFcmTopicUseCase {

    /**
     * 회원의 모든 토픽을 구독
     * <p>
     * FCM 토큰 최초 등록 시 또는 새 챌린저 등록 후 호출. member 토픽을 한 번 구독하고, 모든 챌린저의 org 토픽을 구독한다. fcm_token_topic에 이미 존재하는 토픽은 스킵하므로
     * 멱등하게 호출 가능.
     *
     * @param memberId 회원 ID
     */
    void subscribeAllTopicsByMemberId(Long memberId);

    /**
     * 회원의 모든 토픽을 해제
     * <p>
     * 회원 탈퇴 등 전체 구독을 정리할 때 호출. member 토픽을 한 번 해제하고, 모든 챌린저의 org 토픽을 해제한다.
     *
     * @param memberId 회원 ID
     */
    void unsubscribeAllTopicsByMemberId(Long memberId);

    /**
     * 특정 FCM 토큰을 모든 구독 토픽에서 해제
     * <p>
     * FCM 토큰 갱신 시 이전 토큰 정리 용도로 Outbox 처리에서 호출. fcm_token_topic DB 조회 기반으로 동작하므로 챌린저 정보 재계산 불필요. 해제 후 fcm_token_topic
     * 레코드를 삭제하여 새 토큰 재구독 시 깨끗한 상태를 보장.
     *
     * @param fcmToken 해제할 FCM 토큰 (이전 토큰 문자열)
     * @param memberId 회원 ID
     */
    void unsubscribeTokenFromTopics(String fcmToken, Long memberId);

    /**
     * prefix 없이 구독된 레거시 토픽을 일괄 해제
     * <p>
     * prefix 도입 이전 환경에서 구독된 토픽 정리용 마이그레이션 API에서 호출. DB에 해당 구독 기록이 없으므로 FCM 해제만 수행하며, FCM은 미구독 토픽에 대한 해제를 무시하므로 반복 호출해도
     * 안전.
     *
     * @param memberId 회원 ID
     */
    void unsubscribeLegacyTopics(Long memberId);

    void subscribeToTopic(List<String> fcmTokens, String topic);

    void unsubscribeFromTopic(List<String> fcmTokens, String topic);

    void resubscribeAllLegacyTopics();
}
