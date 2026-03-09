package com.umc.product.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.TopicManagementResponse;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.out.LoadFcmOutboxPort;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.domain.FcmOutbox;
import com.umc.product.notification.domain.FcmOutboxEventType;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.FcmTokenFixture;
import com.umc.product.support.fixture.MemberFixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class FcmServiceTest extends UseCaseTestSupport {

    @Autowired
    private ManageFcmUseCase manageFcmUseCase;

    @Autowired
    private LoadFcmPort loadFcmPort;

    @Autowired
    private LoadFcmOutboxPort loadFcmOutboxPort;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private FcmTokenFixture fcmTokenFixture;

    @Test
    void 신규_토큰_등록_시_FCM_토큰이_저장되고_SUBSCRIBE_이벤트만_생성된다() {
        // given
        Long memberId = memberFixture.일반_멤버("테스터").getId();
        FcmRegistrationRequest request = new FcmRegistrationRequest("new-token");

        // when
        manageFcmUseCase.registerFcmToken(memberId, request);

        // then
        Optional<FcmToken> savedToken = loadFcmPort.findOptionalByMemberId(memberId);
        assertThat(savedToken).isPresent();
        assertThat(savedToken.get().getFcmToken()).isEqualTo("new-token");

        List<FcmOutbox> outboxEvents = loadFcmOutboxPort.findPendingEvents();
        assertThat(outboxEvents).hasSize(1);
        assertThat(outboxEvents.get(0).getEventType()).isEqualTo(FcmOutboxEventType.FCM_SUBSCRIBE);
        assertThat(outboxEvents.get(0).getMemberId()).isEqualTo(memberId);
    }

    @Test
    void 기존_토큰_업데이트_시_토큰이_변경되고_UNSUBSCRIBE_SUBSCRIBE_이벤트가_생성된다() throws Exception {
        // given
        Long memberId = memberFixture.일반_멤버("테스터").getId();
        fcmTokenFixture.FCM_토큰(memberId, "old-token");

        TopicManagementResponse response = mockTopicManagementResponse();
        given(firebaseMessaging.unsubscribeFromTopic(anyList(), anyString()))
                .willReturn(response);

        FcmRegistrationRequest request = new FcmRegistrationRequest("new-token");

        // when
        manageFcmUseCase.registerFcmToken(memberId, request);

        // then
        FcmToken updatedToken = loadFcmPort.findByMemberId(memberId);
        assertThat(updatedToken.getFcmToken()).isEqualTo("new-token");

        List<FcmOutbox> outboxEvents = loadFcmOutboxPort.findPendingEvents();
        assertThat(outboxEvents).hasSize(2);
        assertThat(outboxEvents).extracting(FcmOutbox::getEventType)
                .containsExactlyInAnyOrder(
                        FcmOutboxEventType.FCM_UNSUBSCRIBE,
                        FcmOutboxEventType.FCM_SUBSCRIBE
                );

        FcmOutbox unsubscribeEvent = outboxEvents.stream()
                .filter(e -> e.getEventType() == FcmOutboxEventType.FCM_UNSUBSCRIBE)
                .findFirst().orElseThrow();
        assertThat(unsubscribeEvent.getPayload()).isEqualTo("old-token");
    }

    private TopicManagementResponse mockTopicManagementResponse() throws Exception {
        TopicManagementResponse response = org.mockito.Mockito.mock(TopicManagementResponse.class);
        given(response.getSuccessCount()).willReturn(1);
        given(response.getFailureCount()).willReturn(0);
        return response;
    }
}
