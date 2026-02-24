package com.umc.product.notification.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.ManageFcmTopicUseCase;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.exception.FcmDomainException;
import com.umc.product.notification.domain.exception.FcmErrorCode;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FcmRegistrationServiceTest {

    @Mock ManageFcmUseCase manageFcmUseCase;
    @Mock ManageFcmTopicUseCase manageFcmTopicUseCase;
    @Mock LoadFcmPort loadFcmPort;

    @InjectMocks FcmRegistrationService sut;

    private FcmToken createFcmToken(String token) {
        try {
            Constructor<FcmToken> constructor = FcmToken.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FcmToken fcmToken = constructor.newInstance();
            ReflectionTestUtils.setField(fcmToken, "fcmToken", token);
            return fcmToken;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class refreshTokenAndSubscriptions {

        @Test
        void 구독해제_토큰갱신_재구독_순서로_정상_수행된다() {
            // given
            FcmToken existingToken = createFcmToken("old-token");
            given(loadFcmPort.findByMemberId(1L)).willReturn(existingToken);
            willDoNothing().given(manageFcmTopicUseCase).unsubscribeAllTopicsByMemberId(1L);
            willDoNothing().given(manageFcmTopicUseCase).subscribeAllTopicsByMemberId(1L);

            // when
            sut.refreshTokenAndSubscriptions(1L, new FcmRegistrationRequest("new-token"));

            // then
            InOrder inOrder = inOrder(manageFcmTopicUseCase, manageFcmUseCase);
            inOrder.verify(manageFcmTopicUseCase).unsubscribeAllTopicsByMemberId(1L);
            inOrder.verify(manageFcmUseCase).registerFcmToken(1L, new FcmRegistrationRequest("new-token"));
            inOrder.verify(manageFcmTopicUseCase).subscribeAllTopicsByMemberId(1L);
        }

        @Test
        void 구독해제_실패해도_토큰갱신과_재구독이_진행된다() {
            // given
            FcmToken existingToken = createFcmToken("old-token");
            given(loadFcmPort.findByMemberId(1L)).willReturn(existingToken);
            willThrow(new FcmDomainException(FcmErrorCode.TOPIC_UNSUBSCRIBE_FAILED))
                    .given(manageFcmTopicUseCase).unsubscribeAllTopicsByMemberId(1L);
            willDoNothing().given(manageFcmTopicUseCase).subscribeAllTopicsByMemberId(1L);

            // when
            sut.refreshTokenAndSubscriptions(1L, new FcmRegistrationRequest("new-token"));

            // then
            then(manageFcmUseCase).should().registerFcmToken(1L, new FcmRegistrationRequest("new-token"));
            then(manageFcmTopicUseCase).should().subscribeAllTopicsByMemberId(1L);
        }

        @Test
        void 재구독_실패시_보상로직으로_이전_토큰_복구_및_재구독을_시도한다() {
            // given
            FcmToken existingToken = createFcmToken("old-token");
            // 첫 번째 호출: refreshTokenAndSubscriptions 시작 시 기존 토큰 조회
            // 두 번째 호출: 보상 로직에서 토큰 복구를 위한 조회
            given(loadFcmPort.findByMemberId(1L)).willReturn(existingToken);
            willDoNothing().given(manageFcmTopicUseCase).unsubscribeAllTopicsByMemberId(1L);
            willThrow(new FcmDomainException(FcmErrorCode.TOPIC_SUBSCRIBE_FAILED))
                    .willDoNothing()
                    .given(manageFcmTopicUseCase).subscribeAllTopicsByMemberId(1L);

            // when & then
            assertThatThrownBy(() ->
                    sut.refreshTokenAndSubscriptions(1L, new FcmRegistrationRequest("new-token")))
                    .isInstanceOf(FcmDomainException.class);

            // 보상 로직: 토큰 복구를 위해 loadFcmPort.findByMemberId가 추가 호출됨
            then(loadFcmPort).should(times(2)).findByMemberId(1L);
            // 보상 로직: 재구독 재시도
            then(manageFcmTopicUseCase).should(times(2)).subscribeAllTopicsByMemberId(1L);
        }
    }
}
