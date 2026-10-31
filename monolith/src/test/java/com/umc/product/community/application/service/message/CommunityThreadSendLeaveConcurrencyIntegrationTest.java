package com.umc.product.community.application.service.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import com.umc.product.community.application.port.in.command.thread.dto.CommunityThreadMemberLifecycleInfo;
import com.umc.product.community.application.port.in.command.thread.dto.ThreadActorCommand;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadMessageMutationInfo;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace.TransactionCall;

// 이 테스트는 outbox 행이 만들어졌는지만 센다. 컨텍스트의 outbox poller와 커밋 직후 즉시 전달 worker가 켜져 있으면
// 테스트가 끝난 뒤에도 worker가 DB를 쓰고 있어, afterEach의 TRUNCATE와 락을 서로 기다리다 교착이 날 수 있다.
// 그래서 이 테스트에서는 relay를 끈다(별도 컨텍스트가 하나 더 생긴다).
@TestPropertySource(properties = "app.event-outbox.relay-enabled=false")
@DisplayName("Community thread send/leave 실제 transaction 동시성")
class CommunityThreadSendLeaveConcurrencyIntegrationTest
    extends CommunityThreadConcurrencyIntegrationSupport {

    @Test
    @DisplayName("leave commit 뒤 send는 거절되고 퇴장 기록 외에 unread와 message-created event를 남기지 않는다")
    void sendWaitingForLeave_doesNotLeakUnreadOrMessageEvent() throws Exception {
        ThreadContext thread = createThread();
        createThreadMessageUseCase.create(communityText(
            thread,
            OWNER_ID,
            UUID.fromString("b13f39d5-12f1-49d5-9947-a0d90b996ab3")
        ));
        long messagesBefore = chatMessageRepository.count();
        long outboxesBefore = eventOutboxRepository.count();
        long chatEventsBefore = countEvent("chat.message.created");
        long communityEventsBefore = countEvent("community.thread.message.created");
        long leaveEventsBefore = countEvent("community.thread.member.left");
        CountDownLatch leaveApplied = new CountDownLatch(1);
        CountDownLatch allowLeaveCommit = new CountDownLatch(1);
        TransactionCall<CommunityThreadMemberLifecycleInfo> leave = race.submit(() -> {
            CommunityThreadMemberLifecycleInfo result = leaveThreadUseCase.leave(
                new ThreadActorCommand(thread.threadId(), MEMBER_ID)
            );
            leaveApplied.countDown();
            race.await(allowLeaveCommit, "community leave commit release");
            return result;
        });
        race.await(leaveApplied, "community leave mutation");

        TransactionCall<CommunityThreadMessageMutationInfo> send = race.submit(() ->
            createThreadMessageUseCase.create(communityText(
                thread,
                MEMBER_ID,
                UUID.fromString("9a6e4dde-593c-4f40-a70b-021231f28d7e")
            ))
        );
        try {
            race.awaitPostgreSqlLockWait(send);
            assertThat(send.future().isDone()).isFalse();
        } finally {
            allowLeaveCommit.countDown();
        }
        leave.future().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        ExecutionException failure = assertThrows(
            ExecutionException.class,
            () -> send.future().get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        );
        assertThat(failure.getCause())
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((CommunityDomainException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_ACCESS_DENIED);
        // leave가 남기는 MEMBER_LEFT 퇴장 기록 1건만 추가된다.
        assertThat(chatMessageRepository.count()).isEqualTo(messagesBefore + 1L);
        assertThat(eventOutboxRepository.count()).isEqualTo(outboxesBefore + 2L);
        assertThat(countEvent("chat.message.created")).isEqualTo(chatEventsBefore + 1L);
        assertThat(countEvent("community.thread.message.created")).isEqualTo(communityEventsBefore);
        assertThat(countEvent("community.thread.member.left")).isEqualTo(leaveEventsBefore + 1L);
        assertThat(threadMember(thread.threadId(), OWNER_ID).getUnreadCount()).isZero();
        assertThat(threadMember(thread.threadId(), MEMBER_ID).getUnreadCount()).isEqualTo(1L);
        assertThat(threadMember(thread.threadId(), MEMBER_ID).getState())
            .isEqualTo(CommunityThreadMemberState.LEFT);
        assertThat(loadChatMemberPort.existsByRoomIdAndMemberId(thread.roomId(), MEMBER_ID))
            .isFalse();
    }
}
