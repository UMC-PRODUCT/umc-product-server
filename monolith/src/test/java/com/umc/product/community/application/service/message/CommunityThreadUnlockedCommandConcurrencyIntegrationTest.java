package com.umc.product.community.application.service.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.umc.product.chat.application.port.in.command.UpdateChatReadUseCase;
import com.umc.product.chat.application.port.in.command.dto.UpdateChatReadCommand;
import com.umc.product.community.application.port.in.command.thread.message.ManageCommunityThreadMessageReactionUseCase;
import com.umc.product.community.application.port.in.command.thread.message.dto.ChangeCommunityThreadMessageReactionCommand;
import com.umc.product.community.application.port.in.query.thread.message.dto.CommunityThreadReactionMutationInfo;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace.TransactionCall;

@DisplayName("방 배타 락을 제거한 명령의 실제 트랜잭션 동시성")
@TestPropertySource(properties = "app.event-outbox.relay-enabled=false")
class CommunityThreadUnlockedCommandConcurrencyIntegrationTest
    extends CommunityThreadConcurrencyIntegrationSupport {

    @Autowired
    ManageCommunityThreadMessageReactionUseCase reactionUseCase;
    @Autowired
    UpdateChatReadUseCase updateChatReadUseCase;

    @Test
    @DisplayName("리액션과 Chat 읽음은 다른 트랜잭션이 보유한 두 방의 배타 락을 기다리지 않는다")
    void reactionAndChatRead_completeWhileRoomLocksAreHeld() throws Exception {
        // Given
        ThreadContext thread = createThread();
        Long messageId = createThreadMessageUseCase.create(
            communityText(thread, OWNER_ID, UUID.randomUUID())
        ).message().messageId();
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        TransactionCall<Long> blocker = race.submit(() -> {
            jdbcTemplate.queryForObject(
                "SELECT id FROM community_thread WHERE id = ? FOR UPDATE",
                Long.class,
                thread.threadId()
            );
            jdbcTemplate.queryForObject(
                "SELECT id FROM chat_room WHERE id = ? FOR UPDATE",
                Long.class,
                thread.roomId()
            );
            locked.countDown();
            race.await(release, "방 락 해제");
            return thread.roomId();
        });
        race.await(locked, "두 방의 배타 락 획득");

        // When
        TransactionCall<CommunityThreadReactionMutationInfo> changed = race.submit(() -> {
            CommunityThreadReactionMutationInfo result = reactionUseCase.add(reaction(thread, messageId));
            updateChatReadUseCase.update(new UpdateChatReadCommand(thread.roomId(), MEMBER_ID, messageId));
            return result;
        });
        try {
            // Then: 락을 해제하기 전에 실제 저장과 커밋이 끝나야 한다.
            CommunityThreadReactionMutationInfo result = changed.future().get(
                TIMEOUT_SECONDS / 2,
                TimeUnit.SECONDS
            );
            assertThat(result.deduplicated()).isFalse();
            assertThat(loadChatMemberPort.getByRoomIdAndMemberId(thread.roomId(), MEMBER_ID)
                .getLastReadMessageId()).isEqualTo(messageId);
        } finally {
            release.countDown();
            blocker.future().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("같은 회원의 같은 리액션을 동시에 추가해도 한 행만 저장한다")
    void concurrentReactionAdds_storeOneRow() throws Exception {
        // Given
        ThreadContext thread = createThread();
        Long messageId = createThreadMessageUseCase.create(
            communityText(thread, OWNER_ID, UUID.randomUUID())
        ).message().messageId();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        // When
        List<TransactionCall<CommunityThreadReactionMutationInfo>> calls = List.of(
            concurrentReaction(thread, messageId, ready, start),
            concurrentReaction(thread, messageId, ready, start)
        );
        race.await(ready, "리액션 동시 추가 준비");
        start.countDown();
        CommunityThreadReactionMutationInfo first = calls.get(0).future().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        CommunityThreadReactionMutationInfo second = calls.get(1).future().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Then
        assertThat(List.of(first.deduplicated(), second.deduplicated()))
            .containsExactlyInAnyOrder(false, true);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT count(*) FROM chat_message_reaction WHERE message_id = ? AND member_id = ? AND emoji = ?",
            Long.class,
            messageId,
            MEMBER_ID,
            "👍"
        )).isEqualTo(1L);
    }

    @Test
    @DisplayName("서로 다른 읽음 위치를 동시에 저장해도 DB에는 더 큰 위치가 남는다")
    void concurrentChatReads_keepGreatestWatermark() throws Exception {
        // Given
        ThreadContext thread = createThread();
        Long earlier = createThreadMessageUseCase.create(
            communityText(thread, OWNER_ID, UUID.randomUUID())
        ).message().messageId();
        Long later = createThreadMessageUseCase.create(
            communityText(thread, OWNER_ID, UUID.randomUUID())
        ).message().messageId();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        // When
        var first = race.submit(() -> {
            ready.countDown();
            race.await(start, "낮은 읽음 위치 시작");
            return updateChatReadUseCase.update(new UpdateChatReadCommand(thread.roomId(), MEMBER_ID, earlier));
        });
        var second = race.submit(() -> {
            ready.countDown();
            race.await(start, "높은 읽음 위치 시작");
            return updateChatReadUseCase.update(new UpdateChatReadCommand(thread.roomId(), MEMBER_ID, later));
        });
        race.await(ready, "읽음 동시 갱신 준비");
        start.countDown();
        first.future().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        second.future().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Then
        assertThat(loadChatMemberPort.getByRoomIdAndMemberId(thread.roomId(), MEMBER_ID)
            .getLastReadMessageId()).isEqualTo(later);
    }

    private TransactionCall<CommunityThreadReactionMutationInfo> concurrentReaction(
        ThreadContext thread,
        Long messageId,
        CountDownLatch ready,
        CountDownLatch start
    ) {
        return race.submit(() -> {
            ready.countDown();
            race.await(start, "리액션 동시 추가 시작");
            return reactionUseCase.add(reaction(thread, messageId));
        });
    }

    private ChangeCommunityThreadMessageReactionCommand reaction(ThreadContext thread, Long messageId) {
        return new ChangeCommunityThreadMessageReactionCommand(thread.threadId(), messageId, MEMBER_ID, "👍");
    }
}
