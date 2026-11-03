package com.umc.product.report.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportAction;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetRemoval;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import({ReportPersistenceAdapter.class, ReportActionPersistenceAdapter.class})
@DisplayName("ReportActionPersistenceAdapter")
class ReportActionPersistenceAdapterTest {

    private static final Instant ACTED_AT = Instant.parse("2026-10-29T00:00:00Z");

    @Autowired
    ReportPersistenceAdapter reportPersistenceAdapter;

    @Autowired
    ReportActionPersistenceAdapter sut;

    @Autowired
    ReportActionRepository reportActionRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("잠금 조회한 신고의 처리 상태와 조치 이력을 함께 저장한다")
    void save_처리_상태와_조치_이력을_저장한다() {
        // given
        Long reportId = reportPersistenceAdapter.save(Report.create(1L, ReportTargetType.POST, 2L, null)).getId();
        entityManager.clear();
        Report report = reportPersistenceAdapter.findByIdForUpdate(reportId).orElseThrow();

        // when
        ReportAction action = report.moderate(
            ReportActionType.REJECT, ReportActionReason.OTHER_REJECTION, "신고 사유 아님", ReportTargetRemoval.NONE,
            "1234567890", "모루", ACTED_AT
        );
        reportPersistenceAdapter.save(report);
        sut.save(action);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(reportPersistenceAdapter.findById(reportId).orElseThrow().getStatus())
            .isEqualTo(ReportStatus.REJECTED);
        List<ReportAction> actions = reportActionRepository.findAllByReportIdOrderByActedAtAsc(reportId);
        assertThat(actions).singleElement().satisfies(saved -> {
            assertThat(saved.getActionType()).isEqualTo(ReportActionType.REJECT);
            assertThat(saved.getReasonCode()).isEqualTo(ReportActionReason.OTHER_REJECTION);
            assertThat(saved.getMemo()).isEqualTo("신고 사유 아님");
            assertThat(saved.getTargetRemoval()).isEqualTo(ReportTargetRemoval.NONE);
            assertThat(saved.getModeratorExternalId()).isEqualTo("1234567890");
            assertThat(saved.getModeratorName()).isEqualTo("모루");
            assertThat(saved.getActedAt()).isEqualTo(ACTED_AT);
        });
    }
}
