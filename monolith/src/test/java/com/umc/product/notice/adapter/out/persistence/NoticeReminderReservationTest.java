package com.umc.product.notice.adapter.out.persistence;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@ActiveProfiles("test")
@Import({NoticePersistenceAdapter.class, NoticeQueryRepository.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class NoticeReminderReservationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 24);

    @Autowired
    private SaveNoticePort saveNoticePort;
    @Autowired
    private TransactionTemplate transaction;

    private Notice notice;

    @BeforeEach
    void setUp() {
        notice = transaction.execute(status -> saveNoticePort.save(Notice.create("공지", "내용", 1L, false, false)));
    }

    @AfterEach
    void tearDown() {
        transaction.executeWithoutResult(status -> saveNoticePort.delete(notice));
    }

    @Test
    @DisplayName("같은 공지의 같은 날짜에 동시에 재알림을 요청해도 하나만 선점한다")
    void 동시_재알림은_한번만_선점한다() throws Exception {
        // given
        CyclicBarrier start = new CyclicBarrier(2);
        Callable<Boolean> request = () -> {
            start.await(10, SECONDS);
            return reserve(TODAY);
        };

        // when
        try (ExecutorService workers = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = workers.submit(request);
            Future<Boolean> second = workers.submit(request);

            // then
            assertThat(List.of(first.get(10, SECONDS), second.get(10, SECONDS)))
                .containsExactlyInAnyOrder(true, false);
        }
    }

    @Test
    @DisplayName("실패한 트랜잭션은 당일 한도를 소모하지 않고 다음 날짜에는 다시 보낼 수 있다")
    void 재시도와_날짜별_선점() {
        // given
        transaction.executeWithoutResult(status -> {
            assertThat(saveNoticePort.tryReserveReminder(notice.getId(), TODAY)).isTrue();
            status.setRollbackOnly();
        });

        // when & then
        assertThat(reserve(TODAY)).isTrue();
        assertThat(reserve(TODAY)).isFalse();
        assertThat(reserve(TODAY.plusDays(1))).isTrue();
        assertThat(reserve(TODAY)).isFalse();
    }

    private boolean reserve(LocalDate date) {
        return transaction.execute(status -> saveNoticePort.tryReserveReminder(notice.getId(), date));
    }
}
