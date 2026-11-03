package com.umc.product.report.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({ReportPersistenceAdapter.class, ReportActionPersistenceAdapter.class})
@DisplayName("재전송 대상 신고 조회")
class ReportUndeliveredQueryTest {

    @Autowired
    ReportPersistenceAdapter sut;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("기준 시각 이전에 접수된 FAILED와 한 번도 시도하지 않은 PENDING만 오래된 순으로 limit건 조회한다")
    void findRedeliveryCandidates_재전송_대상만_조회한다() {
        // given
        Long pendingId = save(Report.create(1L, ReportTargetType.POST, 1L, null));
        Report failed = Report.create(1L, ReportTargetType.POST, 2L, null);
        failed.recordDeliveryFailure(1);
        Long failedId = save(failed);
        Report sent = Report.create(1L, ReportTargetType.POST, 3L, null);
        sent.markDelivered(Instant.now());
        save(sent);
        Report legacy = Report.create(1L, ReportTargetType.POST, 4L, null);
        ReflectionTestUtils.setField(legacy, "deliveryStatus", null);
        save(legacy);
        Long laterPendingId = save(Report.create(1L, ReportTargetType.POST, 5L, null));
        Report retrying = Report.create(1L, ReportTargetType.POST, 6L, null);
        retrying.recordDeliveryFailure(5);
        save(retrying);
        // 이미 처리된 신고는 전달 상태가 FAILED여도 다시 보내지 않는다.
        Report moderated = Report.create(1L, ReportTargetType.POST, 7L, null);
        moderated.recordDeliveryFailure(1);
        ReflectionTestUtils.setField(moderated, "status", ReportStatus.APPROVED);
        save(moderated);
        entityManager.clear();
        Instant future = Instant.now().plus(Duration.ofHours(1));

        // when
        List<Report> all = sut.findRedeliveryCandidates(future, 100);
        List<Report> limited = sut.findRedeliveryCandidates(future, 2);
        List<Report> none = sut.findRedeliveryCandidates(Instant.now().minus(Duration.ofHours(1)), 100);

        // then
        assertThat(all).extracting(Report::getId).containsExactly(pendingId, failedId, laterPendingId);
        assertThat(limited).extracting(Report::getId).containsExactly(pendingId, failedId);
        assertThat(none).isEmpty();
    }

    private Long save(Report report) {
        Long id = sut.save(report).getId();
        entityManager.flush();
        return id;
    }
}
