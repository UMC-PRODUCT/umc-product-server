package com.umc.product.notification.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.global.config.FcmProperties;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.notification.application.port.in.command.RecordNotificationHistoryUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.service.FcmAudienceResolver;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.NotificationHistory;

@DisplayName("FCM 알림 요청 이벤트 리스너")
class FcmNotificationRequestedEventListenerTest {

    @Test
    @DisplayName("활성 토큰을 500개 단위 배치 이벤트로 분리한다")
    void batch_event_분리() {
        // given
        FakeLoadFcmPort loadFcmPort = new FakeLoadFcmPort(createTokens(501));
        FakeDomainEventPublisher eventPublisher = new FakeDomainEventPublisher();
        RecordNotificationHistoryUseCase recordHistory = mock(RecordNotificationHistoryUseCase.class);
        FcmNotificationRequestedEventListener listener = new FcmNotificationRequestedEventListener(
            new FcmProperties(true, true),
            new FcmAudienceResolver(null, null, null),
            loadFcmPort,
            eventPublisher,
            recordHistory
        );
        FcmNotificationRequestedEvent event = requestEvent();

        // when
        listener.handle(event);

        // then
        verify(recordHistory).recordHistory(expectedHistory(event), event.memberIds());
        assertThat(eventPublisher.published).hasSize(2);
        assertThat((FcmSendBatchRequestedEvent) eventPublisher.published.get(0))
            .extracting(batch -> batch.tokenIds().size())
            .isEqualTo(500);
        assertThat((FcmSendBatchRequestedEvent) eventPublisher.published.get(1))
            .extracting(batch -> batch.tokenIds().size())
            .isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("활성 토큰이 없거나 FCM이 비활성화되어도 대상 회원의 알림을 저장한다")
    void 발송할_토큰이_없어도_알림_이력_저장(boolean fcmEnabled) {
        // given
        LoadFcmPort loadFcmPort = mock(LoadFcmPort.class);
        RecordNotificationHistoryUseCase recordHistory = mock(RecordNotificationHistoryUseCase.class);
        FakeDomainEventPublisher eventPublisher = new FakeDomainEventPublisher();
        FcmNotificationRequestedEventListener listener = new FcmNotificationRequestedEventListener(
            new FcmProperties(fcmEnabled, true), new FcmAudienceResolver(null, null, null),
            loadFcmPort, eventPublisher, recordHistory
        );
        FcmNotificationRequestedEvent event = requestEvent();
        if (fcmEnabled) {
            given(loadFcmPort.listActiveByMemberIds(event.memberIds())).willReturn(List.of());
        }

        // when
        listener.handle(event);

        // then
        InOrder order = inOrder(recordHistory, loadFcmPort);
        order.verify(recordHistory).recordHistory(expectedHistory(event), event.memberIds());
        if (fcmEnabled) {
            order.verify(loadFcmPort).listActiveByMemberIds(event.memberIds());
        }
        order.verifyNoMoreInteractions();
        assertThat(eventPublisher.published).isEmpty();
    }

    private NotificationHistory expectedHistory(FcmNotificationRequestedEvent event) {
        return new NotificationHistory(event.requestId(), "제목", "본문", null, null, event.occurredAt());
    }

    private FcmNotificationRequestedEvent requestEvent() {
        return FcmNotificationRequestedEvent.from(null, Instant.parse("2026-11-27T00:00:00Z"),
            RequestFcmNotificationCommand.builder()
                .memberIds(List.of(1L, 2L))
                .title("제목")
                .body("본문")
                .build()
        );
    }

    private List<FcmToken> createTokens(int count) {
        List<FcmToken> tokens = new ArrayList<>();
        for (long id = 1; id <= count; id++) {
            FcmToken token = FcmToken.create(id, "installation-" + id, "token-" + id);
            ReflectionTestUtils.setField(token, "id", id);
            tokens.add(token);
        }
        return tokens;
    }

    private static class FakeLoadFcmPort implements LoadFcmPort {

        private final List<FcmToken> tokens;

        private FakeLoadFcmPort(List<FcmToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Optional<FcmToken> findByInstallationIdForUpdate(String installationId) {
            return tokens.stream().filter(token -> token.isInstalledAs(installationId)).findFirst();
        }

        @Override
        public List<FcmToken> listActiveByMemberId(Long memberId) {
            return List.of();
        }

        @Override
        public List<FcmToken> listActiveByMemberIds(List<Long> memberIds) {
            return tokens;
        }

        @Override
        public List<FcmToken> listActiveByToken(String fcmToken) {
            return List.of();
        }

        @Override
        public List<FcmToken> listActiveByIds(List<Long> ids) {
            return List.of();
        }

        @Override
        public List<FcmToken> listActiveForValidation(Instant validatedBefore, int limit) {
            return List.of();
        }
    }

    private static class FakeDomainEventPublisher implements DomainEventPublisher {

        private final List<DomainEvent> published = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            published.add(event);
        }

        @Override
        public void publishAll(Collection<? extends DomainEvent> events) {
            published.addAll(events);
        }
    }
}
