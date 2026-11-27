package com.umc.product.notification.adapter.out.persistentce;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.umc.product.member.domain.Member;
import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.NotificationHistory;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import(NotificationHistoryPersistenceAdapter.class)
class NotificationHistoryPersistenceAdapterTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    SaveNotificationHistoryPort historyPort;

    @Test
    @DisplayName("회원 행을 삭제해도 알림 이력에는 수신 회원 ID가 남는다")
    void 회원_삭제가_알림_이력에_전파되지_않는다() {
        // given
        Member member = entityManager.persist(Member.create("회원", "수신자", "recipient@test.com", null, null));
        entityManager.flush();
        Long memberId = member.getId();
        NotificationHistory history = new NotificationHistory(
            UUID.randomUUID(), "공지", "본문", null, null, Instant.parse("2026-11-27T00:00:00Z")
        );
        historyPort.saveIfAbsent(history, List.of(memberId));

        // when
        entityManager.remove(member);
        entityManager.flush();

        // then
        assertThat(jdbcTemplate.queryForList(
            "SELECT member_id FROM notification_history WHERE request_id = ?", Long.class, history.requestId()
        )).containsExactly(memberId);
    }

    @Test
    @DisplayName("동일 발송 요청을 재처리하면 회원별 이력이 한 건씩 남고 별도 리마인더는 추가된다")
    void 동일_요청은_중복_저장하지_않는다() {
        // given
        Member first = entityManager.persist(Member.create("회원1", "첫째", "first@test.com", null, null));
        Member second = entityManager.persist(Member.create("회원2", "둘째", "second@test.com", null, null));
        entityManager.flush();
        NotificationHistory notice = new NotificationHistory(
            UUID.randomUUID(), "공지", "본문", null, "umc://notice/42",
            Instant.parse("2026-11-27T00:00:00Z")
        );
        NotificationHistory reminder = new NotificationHistory(
            UUID.randomUUID(), "공지 리마인더", "본문", null, notice.deepLink(),
            notice.createdAt().plusSeconds(60)
        );
        List<Long> memberIds = List.of(first.getId(), second.getId());

        // when
        historyPort.saveIfAbsent(notice, memberIds);
        historyPort.saveIfAbsent(notice, memberIds);
        historyPort.saveIfAbsent(reminder, List.of(first.getId()));

        // then
        assertThat(jdbcTemplate.queryForList(
            "SELECT member_id FROM notification_history WHERE request_id = ?", Long.class, notice.requestId()
        )).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(jdbcTemplate.queryForList(
            "SELECT title FROM notification_history WHERE member_id = ? ORDER BY created_at DESC",
            String.class, first.getId()
        )).containsExactly("공지 리마인더", "공지");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT image_url FROM notification_history WHERE request_id = ?", String.class, reminder.requestId()
        )).isNull();
    }
}
