package com.umc.product.report.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.community.adapter.out.persistence.CommunityThreadRepository;
import com.umc.product.community.domain.CommunityThread;
import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportTargetSnapshot;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({ReportPersistenceAdapter.class, ReportActionPersistenceAdapter.class})
@DisplayName("ReportPersistenceAdapter")
class ReportPersistenceAdapterTest {

    private static final Instant NOW = Instant.parse("2026-07-18T00:00:00Z");

    @Autowired
    ReportPersistenceAdapter sut;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    CommunityThreadRepository threadRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("게시글 신고와 스레드 메시지 신고를 member ID 기준으로 같은 테이블에 보존한다")
    void save_게시글과_스레드_메시지_신고를_보존한다() {
        // given
        Report legacy = Report.create(1L, ReportTargetType.COMMENT, 2L, "기존 사유");
        CommunityThread thread = threadRepository.save(createThread(130L));
        Report threadMessage = Report.createThreadMessage(
            3L, thread.getId(), 5L, ReportReason.SPAM, ReportTargetSnapshot.of(7L, "신고 대상 메시지", "스레드 제목")
        );

        // when
        sut.save(legacy);
        sut.save(threadMessage);
        reportRepository.flush();
        Long legacyId = legacy.getId();
        Long threadMessageId = threadMessage.getId();
        entityManager.clear();

        // then
        Report reloadedLegacy = sut.findById(legacyId).orElseThrow();
        Report reloadedThreadMessage = sut.findById(threadMessageId).orElseThrow();
        assertThat(sut.existsByReporterIdAndTargetTypeAndTargetId(1L, ReportTargetType.COMMENT, 2L)).isTrue();
        assertThat(sut.existsByReporterIdAndTargetTypeAndTargetId(3L, ReportTargetType.THREAD_MESSAGE, 5L)).isTrue();
        assertThat(reloadedLegacy.getReason()).isEqualTo("기존 사유");
        assertThat(reloadedThreadMessage.getThreadId()).isEqualTo(thread.getId());
        assertThat(reloadedThreadMessage.getReasonCode()).isEqualTo(ReportReason.SPAM);
        assertThat(reloadedThreadMessage.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
        assertThat(reloadedThreadMessage.getTargetSnapshot().getAuthorId()).isEqualTo(7L);
        assertThat(reloadedThreadMessage.getTargetSnapshot().getContent()).isEqualTo("신고 대상 메시지");
        assertThat(reloadedThreadMessage.getTargetSnapshot().getContext()).isEqualTo("스레드 제목");
        assertThat(reloadedLegacy.getTargetSnapshot()).isNull();
    }

    @Test
    @DisplayName("게시글 중복 신고도 공통 unique로 거절한다")
    void commonUnique_게시글_중복을_거절한다() {
        // given
        Report first = Report.create(6L, ReportTargetType.POST, 7L, null);
        Report second = Report.create(6L, ReportTargetType.POST, 7L, null);

        sut.save(first);

        // when & then
        assertThatThrownBy(() -> sut.save(second))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS)
            );
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("동시 스레드 메시지 중복 신고는 DB 공통 unique로 하나만 저장한다")
    void concurrentDuplicateReport_하나만_저장한다() throws Exception {
        // given
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Long threadId = transactionTemplate.execute(status -> threadRepository.save(createThread(131L)).getId());
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<SaveResult> first = executor.submit(
                () -> saveAfterSignal(transactionTemplate, ready, start, threadId)
            );
            Future<SaveResult> second = executor.submit(
                () -> saveAfterSignal(transactionTemplate, ready, start, threadId)
            );
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();

            // when
            start.countDown();
            List<SaveResult> results = List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));

            // then
            assertThat(results).containsExactlyInAnyOrder(SaveResult.SAVED, SaveResult.DUPLICATE);
            Long count = transactionTemplate.execute(status -> reportRepository
                .countByReporterIdAndTargetTypeAndTargetId(10L, ReportTargetType.THREAD_MESSAGE, 30L));
            assertThat(count).isOne();
        } finally {
            start.countDown();
            executor.shutdownNow();
            transactionTemplate.executeWithoutResult(status -> {
                reportRepository.deleteAllInBatch();
                threadRepository.deleteById(threadId);
            });
        }
    }

    private SaveResult saveAfterSignal(
        TransactionTemplate transactionTemplate,
        CountDownLatch ready,
        CountDownLatch start,
        Long threadId
    ) throws InterruptedException {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("concurrent report start timed out");
        }
        try {
            transactionTemplate.executeWithoutResult(status -> sut.save(
                Report.createThreadMessage(10L, threadId, 30L, ReportReason.ABUSE, null)
            ));
            return SaveResult.SAVED;
        } catch (ReportDomainException exception) {
            assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS);
            return SaveResult.DUPLICATE;
        }
    }

    @Test
    @DisplayName("사용자 신고를 상세 내용·사유·닉네임 스냅샷과 함께 저장하고 다시 조회한다")
    void save_사용자_신고를_보존한다() {
        // given
        Report report = Report.createUser(
            1L, 2L, ReportReason.HARASSMENT, "반복해서 불쾌한 메시지를 보내요.", ReportTargetSnapshot.of(2L, null, "대상닉네임")
        );

        // when
        sut.save(report);
        reportRepository.flush();
        Long reportId = report.getId();
        entityManager.clear();

        // then
        Report reloaded = sut.findById(reportId).orElseThrow();
        assertThat(sut.existsByReporterIdAndTargetTypeAndTargetId(1L, ReportTargetType.USER, 2L)).isTrue();
        assertThat(reloaded.getTargetType()).isEqualTo(ReportTargetType.USER);
        assertThat(reloaded.getTargetId()).isEqualTo(2L);
        assertThat(reloaded.getThreadId()).isNull();
        assertThat(reloaded.getReasonCode()).isEqualTo(ReportReason.HARASSMENT);
        assertThat(reloaded.getReason()).isEqualTo("반복해서 불쾌한 메시지를 보내요.");
        assertThat(reloaded.getTargetSnapshot().getAuthorId()).isEqualTo(2L);
        assertThat(reloaded.getTargetSnapshot().getContent()).isNull();
        assertThat(reloaded.getTargetSnapshot().getContext()).isEqualTo("대상닉네임");
    }

    @Test
    @DisplayName("같은 신고자가 같은 사용자를 다시 신고하면 REPORT_ALREADY_EXISTS로 거부한다")
    void save_사용자_중복_신고를_거부한다() {
        // given
        sut.save(Report.createUser(1L, 2L, ReportReason.SPAM, "첫 신고", null));
        reportRepository.flush();

        // when & then
        assertThatThrownBy(() -> sut.save(Report.createUser(1L, 2L, ReportReason.ETC, "다시 신고", null)))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_EXISTS)
            );
    }

    private enum SaveResult {
        SAVED,
        DUPLICATE
    }

    private CommunityThread createThread(Long chatRoomId) {
        return CommunityThread.create(
            chatRoomId,
            "신고 스레드",
            null,
            CommunityThreadCategory.FREE,
            "🚨",
            10L,
            NOW
        );
    }
}
