package com.umc.product.notification.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.FcmTopicName;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTopicService implements ManageFcmTopicUseCase {

    private final ManageFcmUseCase manageFcmUseCase;
    private final LoadFcmPort loadFcmPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;

    @Override
    public void subscribeTopics(Long challengerId) {
        ChallengerInfo challenger = getChallengerUseCase.getChallengerPublicInfo(challengerId);

        FcmToken fcmToken = loadFcmPort.findOptionalByMemberId(challenger.memberId())
                .orElse(null);
        if (fcmToken == null || fcmToken.getFcmToken().isBlank()) {
            log.warn("FCM 토큰이 없어 토픽 구독을 건너뜁니다. challengerId={}", challengerId);
            return;
        }

        List<String> tokens = List.of(fcmToken.getFcmToken());
        List<String> topics = resolveTopicsForChallenger(challenger);

        for (String topic : topics) {
            manageFcmUseCase.subscribeToTopic(tokens, topic);
        }

        log.info("토픽 구독 완료 challengerId={}, topics={}", challengerId, topics);
    }

    @Override
    public void unsubscribeTopics(Long challengerId) {
        ChallengerInfo challenger = getChallengerUseCase.getChallengerPublicInfo(challengerId);

        FcmToken fcmToken = loadFcmPort.findOptionalByMemberId(challenger.memberId())
                .orElse(null);
        if (fcmToken == null || fcmToken.getFcmToken().isBlank()) {
            return;
        }

        List<String> tokens = List.of(fcmToken.getFcmToken());
        List<String> topics = resolveTopicsForChallenger(challenger);

        for (String topic : topics) {
            manageFcmUseCase.unsubscribeFromTopic(tokens, topic);
        }

        log.info("토픽 구독 해제 완료 challengerId={}, topics={}", challengerId, topics);
    }

    @Override
    public void subscribeAllTopicsByMemberId(Long memberId) {
        List<ChallengerInfo> challengers = getChallengerUseCase.getMemberChallengerList(memberId);
        for (ChallengerInfo challenger : challengers) {
            subscribeTopics(challenger.challengerId());
        }
    }

    @Override
    public void unsubscribeAllTopicsByMemberId(Long memberId) {
        List<ChallengerInfo> challengers = getChallengerUseCase.getMemberChallengerList(memberId);
        for (ChallengerInfo challenger : challengers) {
            unsubscribeTopics(challenger.challengerId());
        }
    }

    /**
     * 챌린저 정보를 기반으로 구독해야 할 토픽 목록을 생성 챌린저는 반드시 기수/학교/지부/파트 정보가 모두 존재해야 한다.
     */
    private List<String> resolveTopicsForChallenger(ChallengerInfo challenger) {
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(challenger.memberId());

        if (memberInfo.schoolId() == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND);
        }

        ChapterInfo chapter = getChapterUseCase.byGisuAndSchool(
            challenger.gisuId(), memberInfo.schoolId());

        return FcmTopicName.allTopicsFor(
            challenger.gisuId(),
            challenger.part(),
            memberInfo.schoolId(),
            chapter.id()
        );
    }
}
