package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy.Rule;

@DisplayName("Outbox 이벤트 처리 그룹 분류")
class OutboxEventRoutingPolicyTest {

    private static final OutboxProcessingGroup REALTIME = OutboxProcessingGroup.REALTIME;
    private static final OutboxProcessingGroup BACKGROUND = OutboxProcessingGroup.BACKGROUND;

    @ParameterizedTest
    @ValueSource(strings = {
        "chat.message.created", "chat.message.updated", "chat.message.deleted",
        "chat.message.reaction.changed", "chat.read.updated", "chat.room.pinned-message.changed",
        "chat.future.created", "community.thread.updated", "community.thread.deleted",
        "community.thread.invited", "community.thread.member.left", "community.thread.member.kicked"
    })
    @DisplayName("Chat 및 Community 실시간 전달 이벤트는 실시간 그룹으로 분류한다")
    void 실시간_이벤트_분류(String eventType) {
        // Given
        OutboxEventRoutingPolicy policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();
        // When & Then
        assertThat(policy.resolve(eventType)).isEqualTo(REALTIME);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "community.thread.message.created", "community.thread.message.mentioned",
        "notification.fcm.requested", "notification.fcm.batch.requested", "unknown.created",
        "chatbot.message.created", "community.thread.updated.extra"
    })
    @DisplayName("알림과 미등록 이벤트는 백그라운드 그룹으로 분류한다")
    void 백그라운드_이벤트_분류(String eventType) {
        OutboxEventRoutingPolicy policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();

        assertThat(policy.resolve(eventType)).isEqualTo(BACKGROUND);
    }

    @Test
    @DisplayName("명시적인 타입 분류는 Chat 기본 규칙보다 우선하고 새 그룹을 지원한다")
    void 새_그룹과_우선순위() {
        // Given
        OutboxProcessingGroup analytics = new OutboxProcessingGroup("analytics");
        OutboxEventRoutingPolicy policy = new OutboxEventRoutingPolicy(
            Set.of(REALTIME, BACKGROUND, analytics), BACKGROUND, List.of(
                new Rule("chat.statistics.completed", analytics),
                new Rule("chat.statistics.requested", BACKGROUND)
            )
        );
        // When & Then
        assertThat(policy.resolve("chat.statistics.completed")).isEqualTo(analytics);
        assertThat(policy.resolve("chat.statistics.requested")).isEqualTo(BACKGROUND);
        assertThat(policy.resolve("chat.statistics.other")).isEqualTo(REALTIME);
        assertThat(policy.groups()).containsExactlyInAnyOrder(REALTIME, BACKGROUND, analytics);
    }

    @Test
    @DisplayName("중복 규칙과 등록되지 않은 그룹은 거부한다")
    void 잘못된_그룹_설정_거부() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OutboxEventRoutingPolicy(
            Set.of(REALTIME, BACKGROUND), BACKGROUND,
            List.of(new Rule("chat.message.created", REALTIME), new Rule("chat.message.created", BACKGROUND))
        ));
        assertThatIllegalArgumentException().isThrownBy(() -> new OutboxEventRoutingPolicy(
            Set.of(BACKGROUND), BACKGROUND, List.of(new Rule("chat.message.created", REALTIME))
        ));
        assertThatIllegalArgumentException().isThrownBy(() -> new OutboxEventRoutingPolicy(
            Set.of(REALTIME), BACKGROUND, List.of()
        ));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", " chat.created", "chat.*"})
    @DisplayName("잘못된 이벤트 타입은 기본 그룹으로 숨기지 않고 거부한다")
    void 잘못된_이벤트_타입_거부(String eventType) {
        OutboxEventRoutingPolicy policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();

        assertThatIllegalArgumentException().isThrownBy(() -> policy.resolve(eventType));
    }

    @Test
    @DisplayName("잘못된 그룹 이름과 와일드카드 규칙은 거부한다")
    void 잘못된_이름과_규칙_거부() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OutboxProcessingGroup("REALTIME"));
        assertThatIllegalArgumentException().isThrownBy(() -> new Rule("chat.*", REALTIME));
    }
}
