package com.umc.product.report.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetType;

@DisplayName("Report")
class ReportTest {

    @Test
    @DisplayName("기존 게시글 댓글 신고 모델을 그대로 보존한다")
    void create_기존_신고를_보존한다() {
        // given & when
        Report report = Report.create(1L, ReportTargetType.POST, 2L, "상세 사유");

        // then
        assertThat(report.getReporterId()).isEqualTo(1L);
        assertThat(report.getTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(report.getTargetId()).isEqualTo(2L);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getReason()).isEqualTo("상세 사유");
        assertThat(report.getThreadId()).isNull();
        assertThat(report.getReasonCode()).isNull();
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
        assertThat(report.getDeliveryAttemptCount()).isZero();
        assertThat(report.getTargetSnapshot()).isNull();
    }

    @Test
    @DisplayName("스레드 메시지 신고는 threadId messageId와 typed reason, 신고 시점 스냅샷을 보존한다")
    void createThreadMessage_식별자와_사유를_보존한다() {
        // given & when
        Report report = Report.createThreadMessage(
            1L, 2L, 3L, ReportReason.ABUSE, ReportTargetSnapshot.of(4L, "신고 대상 메시지", "스레드 제목")
        );

        // then
        assertThat(report.getReporterId()).isEqualTo(1L);
        assertThat(report.getTargetType()).isEqualTo(ReportTargetType.THREAD_MESSAGE);
        assertThat(report.getThreadId()).isEqualTo(2L);
        assertThat(report.getTargetId()).isEqualTo(3L);
        assertThat(report.getReasonCode()).isEqualTo(ReportReason.ABUSE);
        assertThat(report.getReason()).isNull();
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
        assertThat(report.getTargetSnapshot().getAuthorId()).isEqualTo(4L);
        assertThat(report.getTargetSnapshot().getContent()).isEqualTo("신고 대상 메시지");
        assertThat(report.getTargetSnapshot().getContext()).isEqualTo("스레드 제목");
    }

    @Test
    @DisplayName("스냅샷 작성자 ID가 없으면 만들 수 없다")
    void snapshot_작성자가_없으면_거부한다() {
        assertThatThrownBy(() -> ReportTargetSnapshot.of(null, "내용", "스레드 제목"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
