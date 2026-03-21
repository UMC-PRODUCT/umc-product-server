package com.umc.product.notification.application.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.LoadFcmTopicPort;
import com.umc.product.notification.application.port.out.SaveFcmTopicPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.FcmTopicName;
import com.umc.product.notification.domain.exception.FcmDomainException;
import com.umc.product.notification.domain.exception.FcmErrorCode;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTopicService implements ManageFcmTopicUseCase {

    private final FirebaseMessaging firebaseMessaging;
    private final LoadFcmPort loadFcmPort;
    private final LoadFcmTopicPort loadFcmTopicPort;
    private final SaveFcmTopicPort saveFcmTopicPort;
    private final FcmTopicName fcmTopicName;

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;

    /**
     * FCM 토큰 최초 등록 시 또는 새 챌린저 등록 후 호출. member 토픽을 한 번 구독하고, 모든 챌린저의 토픽을 구독한다. fcm_token_topic에 이미 존재하는 토픽은 스킵함
     */
    @Override
    @Transactional
    public void subscribeAllTopicsByMemberId(Long memberId) {
        FcmToken fcmToken = loadFcmPort.findOptionalByMemberId(memberId).orElse(null);
        if (fcmToken == null || fcmToken.getFcmToken().isBlank()) {
            log.warn("FCM 토큰이 없어 토픽 구독을 건너뜁니다. memberId={}", memberId);
            return;
        }

        List<String> tokens = List.of(fcmToken.getFcmToken());

        // member 토픽: 멤버 단위로 한 번만 구독 (이미 있으면 스킵)
        String memberTopic = fcmTopicName.member(memberId);
        if (!loadFcmTopicPort.existsByFcmTokenIdAndTopicName(fcmToken.getId(), memberTopic)) {
            subscribeToTopic(tokens, memberTopic);
            saveFcmTopicPort.saveTopicSubscription(fcmToken.getId(), memberTopic);
        }

        // memberInfo는 모든 챌린저가 동일한 memberId를 가지므로 한 번만 조회
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(memberId);
        memberInfo.validateHasSchool();

        List<ChallengerInfo> challengers = getChallengerUseCase.getMemberChallengerList(memberId);
        for (ChallengerInfo challenger : challengers) {
            subscribeChallengerTopics(fcmToken, tokens, challenger, memberInfo);
        }

        log.info("토픽 구독 완료 memberId={}", memberId);
    }

    /**
     * 회원 탈퇴 등 전체 구독을 정리할 때 호출. DB 기반으로 동작하므로 챌린저 데이터가 없어도 안전.
     */
    @Override
    @Transactional
    public void unsubscribeAllTopicsByMemberId(Long memberId) {
        FcmToken fcmToken = loadFcmPort.findOptionalByMemberId(memberId).orElse(null);
        if (fcmToken == null || fcmToken.getFcmToken().isBlank()) {
            return;
        }

        List<String> tokens = List.of(fcmToken.getFcmToken());
        List<String> topics = loadFcmTopicPort.findTopicNamesByFcmTokenId(fcmToken.getId());

        for (String topic : topics) {
            unsubscribeFromTopic(tokens, topic);
        }

        saveFcmTopicPort.deleteAllTopicSubscriptions(fcmToken.getId());

        log.info("토픽 구독 전체 해제 완료 memberId={}, topics={}", memberId, topics);
    }

    /**
     * FCM 토큰 갱신 시 이전 토큰 정리 용도로 Outbox 처리에서 호출. fcm_token_topic DB 조회 기반으로 동작하므로 챌린저 정보 재계산 불필요. 해제 후 fcm_token_topic
     * 레코드를 삭제하여 새 토큰 재구독 시 깨끗한 상태를 보장.
     */
    @Override
    @Transactional
    public void unsubscribeTokenFromTopics(String fcmToken, Long memberId) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return;
        }

        FcmToken tokenEntity = loadFcmPort.findOptionalByMemberId(memberId).orElse(null);
        if (tokenEntity == null) {
            return;
        }

        List<String> tokens = List.of(fcmToken);
        List<String> topics = loadFcmTopicPort.findTopicNamesByFcmTokenId(tokenEntity.getId());

        for (String topic : topics) {
            unsubscribeFromTopic(tokens, topic);
        }

        saveFcmTopicPort.deleteAllTopicSubscriptions(tokenEntity.getId());

        log.info("이전 토큰 토픽 구독 해제 완료 memberId={}, topics={}", memberId, topics);
    }

    /**
     * 마이그레이션 API에서 호출. prefix 도입 이전에 구독된 레거시 토픽(예: "gisu-1", "all")을 일괄 해제. FCM은 미구독 토픽에 대한 해제를 무시하므로 반복 호출해도 안전.
     */
    @Override
    public void unsubscribeLegacyTopics(Long memberId) {
        FcmToken fcmToken = loadFcmPort.findOptionalByMemberId(memberId).orElse(null);
        if (fcmToken == null || fcmToken.getFcmToken().isBlank()) {
            log.warn("FCM 토큰이 없어 레거시 토픽 해제를 건너뜁니다. memberId={}", memberId);
            return;
        }

        // memberInfo는 모든 챌린저가 동일한 memberId를 가지므로 한 번만 조회
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(memberId);
        if (memberInfo.schoolId() == null) {
            log.warn("학교 정보가 없어 레거시 토픽 해제를 건너뜁니다. memberId={}", memberId);
            return;
        }

        List<String> tokens = List.of(fcmToken.getFcmToken());
        List<ChallengerInfo> challengers = getChallengerUseCase.getMemberChallengerList(memberId);

        for (ChallengerInfo challenger : challengers) {
            ChapterInfo chapter = getChapterUseCase.byGisuAndSchool(challenger.gisuId(), memberInfo.schoolId());
            List<String> legacyTopics = fcmTopicName.allTopicsForWithoutPrefix(
                challenger.gisuId(), challenger.part(), memberInfo.schoolId(), chapter.id());

            for (String topic : legacyTopics) {
                unsubscribeFromTopic(tokens, topic);
            }

            log.info("레거시 토픽 구독 해제 완료 challengerId={}, topics={}", challenger.challengerId(), legacyTopics);
        }
    }

    // =========== PRIVATE ==============

    private void subscribeChallengerTopics(FcmToken fcmToken, List<String> tokens, ChallengerInfo challenger, MemberInfo memberInfo) {
        List<String> topics = resolveChallengerTopics(challenger, memberInfo);
        for (String topic : topics) {
            if (!loadFcmTopicPort.existsByFcmTokenIdAndTopicName(fcmToken.getId(), topic)) {
                subscribeToTopic(tokens, topic);
                saveFcmTopicPort.saveTopicSubscription(fcmToken.getId(), topic);
            }
        }
        log.debug("챌린저 토픽 구독 완료 challengerId={}, topics={}", challenger.challengerId(), topics);
    }

    private List<String> resolveChallengerTopics(ChallengerInfo challenger, MemberInfo memberInfo) {
        ChapterInfo chapter = getChapterUseCase.byGisuAndSchool(challenger.gisuId(), memberInfo.schoolId());

        return fcmTopicName.allTopicsFor(
            challenger.gisuId(),
            challenger.part(),
            memberInfo.schoolId(),
            chapter.id()
        );
    }

    @Override
    public void subscribeToTopic(List<String> fcmTokens, String topic) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(fcmTokens, topic);
            log.info("토픽 구독 완료 topic={}, 성공={}, 실패={}",
                topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 구독 실패 topic={}", topic, e);
            throw new FcmDomainException(FcmErrorCode.TOPIC_SUBSCRIBE_FAILED);
        }
    }

    @Override
    public void unsubscribeFromTopic(List<String> fcmTokens, String topic) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(fcmTokens, topic);
            log.info("토픽 구독 해제 완료 topic={}, 성공={}, 실패={}",
                topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 구독 해제 실패 topic={}", topic, e);
            throw new FcmDomainException(FcmErrorCode.TOPIC_UNSUBSCRIBE_FAILED);
        }
    }
}
