package com.umc.product.notification.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.exception.OrganizationDomainException;
import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FcmTopicServiceTest {

    @Mock ManageFcmUseCase manageFcmUseCase;
    @Mock LoadFcmPort loadFcmPort;
    @Mock GetChallengerUseCase getChallengerUseCase;
    @Mock GetMemberUseCase getMemberUseCase;
    @Mock GetChapterUseCase getChapterUseCase;

    @InjectMocks FcmTopicService sut;

    // -- test fixtures --

    private ChallengerInfo challengerInfo(Long challengerId, Long memberId, Long gisuId, ChallengerPart part) {
        return ChallengerInfo.builder()
                .challengerId(challengerId)
                .memberId(memberId)
                .gisuId(gisuId)
                .part(part)
                .build();
    }

    private MemberInfo memberInfo(Long memberId, Long schoolId) {
        return new MemberInfo(memberId, "강하나", "와나", "test@umc.com",
                schoolId, null, MemberStatus.ACTIVE);
    }

    private FcmToken fcmToken(Long memberId, String token) {
        try {
            Constructor<FcmToken> constructor = FcmToken.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FcmToken tokenEntity = constructor.newInstance();
            ReflectionTestUtils.setField(tokenEntity, "fcmToken", token);
            return tokenEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class subscribeTopics {

        @Test
        void 정상적으로_모든_토픽을_구독한다() {
            // given
            Long challengerId = 1L;
            ChallengerInfo challenger = challengerInfo(1L, 100L, 9L, ChallengerPart.SPRINGBOOT);
            given(getChallengerUseCase.getChallengerPublicInfo(challengerId)).willReturn(challenger);

            FcmToken token = fcmToken(100L, "test-fcm-token");
            given(loadFcmPort.findByMemberId(100L)).willReturn(token);

            MemberInfo member = memberInfo(100L, 5L);
            given(getMemberUseCase.getById(100L)).willReturn(member);

            given(getChapterUseCase.byGisuAndSchool(9L, 5L))
                    .willReturn(new ChapterInfo(3L, "cassiopeia"));

            // when
            sut.subscribeTopics(challengerId);

            // then: 기수, 기수+파트, 학교, 학교+파트, 지부, 지부+파트 = 6회
            then(manageFcmUseCase).should(times(6))
                    .subscribeToTopic(eq(List.of("test-fcm-token")), anyString());
        }

        @Test
        void FCM_토큰이_null이면_구독을_건너뛴다() {
            // given
            Long challengerId = 1L;
            ChallengerInfo challenger = challengerInfo(1L, 100L, 9L, ChallengerPart.SPRINGBOOT);
            given(getChallengerUseCase.getChallengerPublicInfo(challengerId)).willReturn(challenger);
            given(loadFcmPort.findByMemberId(100L)).willReturn(null);

            // when
            sut.subscribeTopics(challengerId);

            // then
            then(manageFcmUseCase).should(never()).subscribeToTopic(anyList(), anyString());
        }

        @Test
        void FCM_토큰이_빈_문자열이면_구독을_건너뛴다() {
            // given
            Long challengerId = 1L;
            ChallengerInfo challenger = challengerInfo(1L, 100L, 9L, ChallengerPart.SPRINGBOOT);
            given(getChallengerUseCase.getChallengerPublicInfo(challengerId)).willReturn(challenger);

            FcmToken token = fcmToken(100L, "");
            given(loadFcmPort.findByMemberId(100L)).willReturn(token);

            // when
            sut.subscribeTopics(challengerId);

            // then
            then(manageFcmUseCase).should(never()).subscribeToTopic(anyList(), anyString());
        }

        @Test
        void 학교_정보가_없으면_예외를_던진다() {
            // given
            Long challengerId = 1L;
            ChallengerInfo challenger = challengerInfo(1L, 100L, 9L, ChallengerPart.WEB);
            given(getChallengerUseCase.getChallengerPublicInfo(challengerId)).willReturn(challenger);

            FcmToken token = fcmToken(100L, "test-fcm-token");
            given(loadFcmPort.findByMemberId(100L)).willReturn(token);

            MemberInfo member = memberInfo(100L, null); // schoolId = null
            given(getMemberUseCase.getById(100L)).willReturn(member);

            // when & then
            assertThatThrownBy(() -> sut.subscribeTopics(challengerId))
                    .isInstanceOf(OrganizationDomainException.class);
            then(manageFcmUseCase).should(never()).subscribeToTopic(anyList(), anyString());
        }

        @Test
        void 지부_조회_실패시_예외를_던진다() {
            // given
            Long challengerId = 1L;
            ChallengerInfo challenger = challengerInfo(1L, 100L, 9L, ChallengerPart.ANDROID);
            given(getChallengerUseCase.getChallengerPublicInfo(challengerId)).willReturn(challenger);

            FcmToken token = fcmToken(100L, "test-fcm-token");
            given(loadFcmPort.findByMemberId(100L)).willReturn(token);

            MemberInfo member = memberInfo(100L, 5L);
            given(getMemberUseCase.getById(100L)).willReturn(member);

            given(getChapterUseCase.byGisuAndSchool(9L, 5L))
                    .willThrow(new OrganizationDomainException(
                            com.umc.product.organization.exception.OrganizationErrorCode.CHAPTER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> sut.subscribeTopics(challengerId))
                    .isInstanceOf(OrganizationDomainException.class);
            then(manageFcmUseCase).should(never()).subscribeToTopic(anyList(), anyString());
        }
    }

    @Nested
    class unsubscribeTopics {

        @Test
        void 정상적으로_모든_토픽을_구독_해제한다() {
            // given
            Long challengerId = 1L;
            ChallengerInfo challenger = challengerInfo(1L, 100L, 9L, ChallengerPart.SPRINGBOOT);
            given(getChallengerUseCase.getChallengerPublicInfo(challengerId)).willReturn(challenger);

            FcmToken token = fcmToken(100L, "test-fcm-token");
            given(loadFcmPort.findByMemberId(100L)).willReturn(token);

            MemberInfo member = memberInfo(100L, 5L);
            given(getMemberUseCase.getById(100L)).willReturn(member);

            given(getChapterUseCase.byGisuAndSchool(9L, 5L))
                    .willReturn(new ChapterInfo(3L, "cassiopeia"));

            // when
            sut.unsubscribeTopics(challengerId);

            // then
            then(manageFcmUseCase).should(times(6))
                    .unsubscribeFromTopic(eq(List.of("test-fcm-token")), anyString());
        }

        @Test
        void FCM_토큰이_null이면_구독_해제를_건너뛴다() {
            // given
            Long challengerId = 1L;
            ChallengerInfo challenger = challengerInfo(1L, 100L, 9L, ChallengerPart.SPRINGBOOT);
            given(getChallengerUseCase.getChallengerPublicInfo(challengerId)).willReturn(challenger);
            given(loadFcmPort.findByMemberId(100L)).willReturn(null);

            // when
            sut.unsubscribeTopics(challengerId);

            // then
            then(manageFcmUseCase).should(never()).unsubscribeFromTopic(anyList(), anyString());
        }
    }

    @Nested
    class subscribeAllTopicsByMemberId {

        @Test
        void 회원의_모든_챌린저에_대해_토픽을_구독한다() {
            // given
            Long memberId = 100L;
            ChallengerInfo c1 = challengerInfo(1L, memberId, 9L, ChallengerPart.SPRINGBOOT);
            ChallengerInfo c2 = challengerInfo(2L, memberId, 8L, ChallengerPart.WEB);
            given(getChallengerUseCase.getMemberChallengerList(memberId))
                    .willReturn(List.of(c1, c2));

            // 각 챌린저에 대한 stub
            given(getChallengerUseCase.getChallengerPublicInfo(1L)).willReturn(c1);
            given(getChallengerUseCase.getChallengerPublicInfo(2L)).willReturn(c2);

            FcmToken token = fcmToken(memberId, "test-fcm-token");
            given(loadFcmPort.findByMemberId(memberId)).willReturn(token);

            MemberInfo member = memberInfo(memberId, 5L);
            given(getMemberUseCase.getById(memberId)).willReturn(member);

            given(getChapterUseCase.byGisuAndSchool(9L, 5L))
                    .willReturn(new ChapterInfo(3L, "cassiopeia"));
            given(getChapterUseCase.byGisuAndSchool(8L, 5L))
                    .willReturn(new ChapterInfo(4L, "cassiopeia"));

            // when
            sut.subscribeAllTopicsByMemberId(memberId);

            // then: 각 챌린저당 6개 × 2명 = 12회
            then(manageFcmUseCase).should(times(12))
                    .subscribeToTopic(anyList(), anyString());
        }

        @Test
        void 챌린저가_없으면_아무것도_호출하지_않는다() {
            // given
            given(getChallengerUseCase.getMemberChallengerList(100L))
                    .willReturn(List.of());

            // when
            sut.subscribeAllTopicsByMemberId(100L);

            // then
            then(manageFcmUseCase).should(never()).subscribeToTopic(anyList(), anyString());
        }
    }

    @Nested
    class unsubscribeAllTopicsByMemberId {

        @Test
        void 회원의_모든_챌린저에_대해_토픽을_해제한다() {
            // given
            Long memberId = 100L;
            ChallengerInfo c1 = challengerInfo(1L, memberId, 9L, ChallengerPart.SPRINGBOOT);
            given(getChallengerUseCase.getMemberChallengerList(memberId))
                    .willReturn(List.of(c1));

            given(getChallengerUseCase.getChallengerPublicInfo(1L)).willReturn(c1);

            FcmToken token = fcmToken(memberId, "test-fcm-token");
            given(loadFcmPort.findByMemberId(memberId)).willReturn(token);

            MemberInfo member = memberInfo(memberId, 5L);
            given(getMemberUseCase.getById(memberId)).willReturn(member);

            given(getChapterUseCase.byGisuAndSchool(9L, 5L))
                    .willReturn(new ChapterInfo(3L, "cassiopeia"));

            // when
            sut.unsubscribeAllTopicsByMemberId(memberId);

            // then
            then(manageFcmUseCase).should(times(6))
                    .unsubscribeFromTopic(anyList(), anyString());
        }
    }
}
