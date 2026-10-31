package com.umc.product.report.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetRemoval;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

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
    @DisplayName("작성자가 없는 대상(시스템 메시지)도 스냅샷으로 남길 수 있다")
    void snapshot_작성자가_없어도_만든다() {
        ReportTargetSnapshot snapshot = ReportTargetSnapshot.of(null, "입장했습니다.", "스레드 제목");

        assertThat(snapshot.getAuthorId()).isNull();
        assertThat(snapshot.getContent()).isEqualTo("입장했습니다.");
    }

    @Test
    @DisplayName("스냅샷 작성자 ID가 0 이하면 만들 수 없다")
    void snapshot_작성자_ID가_양수가_아니면_거부한다() {
        assertThatThrownBy(() -> ReportTargetSnapshot.of(0L, "내용", "스레드 제목"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("전달에 성공하면 SENT와 전달 시각을 남긴다")
    void markDelivered_전달_완료를_기록한다() {
        // given
        Report report = Report.create(1L, ReportTargetType.POST, 2L, null);
        Instant deliveredAt = Instant.parse("2026-10-26T00:00:00Z");

        // when
        report.markDelivered(deliveredAt);

        // then
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.SENT);
        assertThat(report.getDeliveredAt()).isEqualTo(deliveredAt);
        assertThat(report.isDeliveryPending()).isFalse();
    }

    @Test
    @DisplayName("전달 실패는 시도 횟수를 올리고 최대 횟수에 닿으면 FAILED로 바꾼다")
    void recordDeliveryFailure_최대_횟수에서_FAILED가_된다() {
        // given
        Report report = Report.create(1L, ReportTargetType.POST, 2L, null);

        // when & then
        report.recordDeliveryFailure(2);
        assertThat(report.getDeliveryAttemptCount()).isEqualTo(1);
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);

        report.recordDeliveryFailure(2);
        assertThat(report.getDeliveryAttemptCount()).isEqualTo(2);
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.FAILED);
        assertThat(report.isDeliveryPending()).isFalse();
    }

    @Test
    @DisplayName("처리 대기 신고를 처리하면 APPROVED로 바꾸고 조치 이력을 만든다")
    void moderate_처리하면_APPROVED와_이력을_남긴다() {
        // given
        Report report = savedReport();
        Instant actedAt = Instant.parse("2026-10-29T00:00:00Z");

        // when
        ReportAction action = report.moderate(
            ReportActionType.APPROVE, ReportActionReason.ABUSE, " 욕설 확인 ", ReportTargetRemoval.DELETED,
            "1234567890", "모루", actedAt
        );

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(action.getReportId()).isEqualTo(901L);
        assertThat(action.getActionType()).isEqualTo(ReportActionType.APPROVE);
        assertThat(action.getReasonCode()).isEqualTo(ReportActionReason.ABUSE);
        assertThat(action.getMemo()).isEqualTo("욕설 확인");
        assertThat(action.getTargetRemoval()).isEqualTo(ReportTargetRemoval.DELETED);
        assertThat(action.getModeratorExternalId()).isEqualTo("1234567890");
        assertThat(action.getModeratorName()).isEqualTo("모루");
        assertThat(action.getActedAt()).isEqualTo(actedAt);
    }

    @Test
    @DisplayName("처리 대기 신고를 반려하면 REJECTED로 바꾼다")
    void moderate_반려하면_REJECTED가_된다() {
        // given
        Report report = savedReport();

        // when
        ReportAction action = report.moderate(
            ReportActionType.REJECT, ReportActionReason.NOT_VIOLATION, "  ", ReportTargetRemoval.NONE,
            "1234567890", "모루", Instant.now()
        );

        // then
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(action.getReasonCode()).isEqualTo(ReportActionReason.NOT_VIOLATION);
        assertThat(action.getMemo()).isNull();
    }

    @Test
    @DisplayName("이미 처리된 신고는 다시 조치할 수 없다")
    void moderate_이미_처리된_신고는_거부한다() {
        // given
        Report report = savedReport();
        report.moderate(
            ReportActionType.APPROVE, ReportActionReason.ABUSE, null, ReportTargetRemoval.NONE, "1", "모루",
            Instant.now()
        );

        // when & then
        assertThatThrownBy(() -> report.moderate(
            ReportActionType.REJECT, ReportActionReason.NOT_VIOLATION, "번복", ReportTargetRemoval.NONE, "2",
            "다른 운영자", Instant.now()
        ))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_MODERATED)
            );
        assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("사유가 없거나 다른 조치의 사유, 메모 없는 기타 사유, 500자를 넘는 메모면 조치하지 않고 상태도 그대로 둔다")
    void moderate_사유와_메모가_잘못되면_거부한다() {
        // given
        Report report = savedReport();
        String tooLong = "가".repeat(ReportAction.MEMO_MAX_LENGTH + 1);

        // when & then
        assertThatThrownBy(() -> report.moderate(
            ReportActionType.APPROVE, null, null, ReportTargetRemoval.NONE, "1", "모루", Instant.now()
        ))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> report.moderate(
            ReportActionType.APPROVE, ReportActionReason.NOT_VIOLATION, null, ReportTargetRemoval.NONE, "1", "모루", Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> report.moderate(
            ReportActionType.REJECT, ReportActionReason.OTHER_REJECTION, " ", ReportTargetRemoval.NONE, "1", "모루", Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> report.moderate(
            ReportActionType.APPROVE, ReportActionReason.SPAM, tooLong, ReportTargetRemoval.NONE, "1", "모루", Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("기타 사유는 메모가 있으면 조치한다")
    void moderate_기타_사유는_메모와_함께_조치한다() {
        // given
        Report report = savedReport();

        // when
        ReportAction action = report.moderate(
            ReportActionType.APPROVE, ReportActionReason.OTHER_VIOLATION, "사칭 의심", ReportTargetRemoval.NONE, "1",
            "모루", Instant.now()
        );

        // then
        assertThat(action.getReasonCode()).isEqualTo(ReportActionReason.OTHER_VIOLATION);
        assertThat(action.getMemo()).isEqualTo("사칭 의심");
    }

    @Test
    @DisplayName("모든 사유는 처리와 반려 중 한 조치에만 속한다")
    void 사유는_조치별로_나뉜다() {
        assertThat(ReportActionReason.values()).filteredOn(reason -> reason.actionType() == ReportActionType.APPROVE)
            .containsExactly(
                ReportActionReason.SPAM,
                ReportActionReason.ABUSE,
                ReportActionReason.INAPPROPRIATE,
                ReportActionReason.PRIVACY,
                ReportActionReason.HARASSMENT,
                ReportActionReason.IMPERSONATION,
                ReportActionReason.OTHER_VIOLATION
            );
        assertThat(ReportActionReason.values()).filteredOn(reason -> reason.actionType() == ReportActionType.REJECT)
            .containsExactly(
                ReportActionReason.NOT_VIOLATION,
                ReportActionReason.CONTENT_MISMATCH,
                ReportActionReason.UNVERIFIABLE,
                ReportActionReason.DUPLICATE,
                ReportActionReason.OTHER_REJECTION
            );
        assertThat(ReportActionReason.values()).filteredOn(ReportActionReason::isMemoRequired)
            .containsExactly(ReportActionReason.OTHER_VIOLATION, ReportActionReason.OTHER_REJECTION);
    }

    @Test
    @DisplayName("전달 실패 신고를 다시 보내도록 되돌리면 PENDING과 실패 횟수 0이 된다")
    void requeueDelivery_실패한_신고를_되돌린다() {
        // given
        Report report = Report.create(1L, ReportTargetType.POST, 2L, null);
        report.recordDeliveryFailure(1);

        // when
        report.requeueDelivery();

        // then
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
        assertThat(report.getDeliveryAttemptCount()).isZero();
    }

    @Test
    @DisplayName("이미 전달한 신고나 전달 기능 이전 신고는 다시 보내도록 되돌릴 수 없다")
    void requeueDelivery_전달_대상이_아니면_거부한다() {
        // given
        Report sent = Report.create(1L, ReportTargetType.POST, 2L, null);
        sent.markDelivered(Instant.parse("2026-10-29T00:00:00Z"));
        Report legacy = Report.create(1L, ReportTargetType.POST, 3L, null);
        ReflectionTestUtils.setField(legacy, "deliveryStatus", null);

        // when & then
        assertThatThrownBy(sent::requeueDelivery).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(legacy::requeueDelivery).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("메모 길이는 문자 단위로 세서 이모지 500개도 받아들인다")
    void moderate_메모_길이는_문자_단위로_센다() {
        // given
        Report report = savedReport();

        // when
        ReportAction action = report.moderate(
            ReportActionType.APPROVE, ReportActionReason.SPAM, "😀".repeat(500), ReportTargetRemoval.NONE, "1",
            "모루", Instant.now()
        );

        // then
        assertThat(action.getMemo()).isEqualTo("😀".repeat(500));
    }

    @Test
    @DisplayName("반려하면서 대상을 삭제한 것으로 기록하면 거부하고 상태도 그대로 둔다")
    void moderate_반려하면서_삭제로_기록하면_거부한다() {
        // given
        Report report = savedReport();

        // when & then
        assertThatThrownBy(() -> report.moderate(
            ReportActionType.REJECT, ReportActionReason.NOT_VIOLATION, null, ReportTargetRemoval.DELETED, "1", "모루",
            Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("삭제 결과가 없으면 조치하지 않는다")
    void moderate_삭제_결과가_없으면_거부한다() {
        // given
        Report report = savedReport();

        // when & then
        assertThatThrownBy(() -> report.moderate(
            ReportActionType.APPROVE, ReportActionReason.ABUSE, null, null, "1", "모루", Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("대상 삭제는 스레드 메시지 신고를 처리할 때만 동반한다")
    void removesTargetOn_스레드_메시지_처리만_true() {
        // given
        Report threadMessage = Report.createThreadMessage(
            1L, 2L, 3L, ReportReason.ABUSE, ReportTargetSnapshot.of(4L, "신고 대상 메시지", "스레드 제목")
        );
        Report post = Report.create(1L, ReportTargetType.POST, 2L, null);
        Report comment = Report.create(1L, ReportTargetType.COMMENT, 2L, null);

        // when & then
        assertThat(threadMessage.removesTargetOn(ReportActionType.APPROVE)).isTrue();
        assertThat(threadMessage.removesTargetOn(ReportActionType.REJECT)).isFalse();
        assertThat(post.removesTargetOn(ReportActionType.APPROVE)).isFalse();
        assertThat(comment.removesTargetOn(ReportActionType.APPROVE)).isFalse();
    }

    @Test
    @DisplayName("처리 대기가 아닌 신고는 requirePending에서 거부한다")
    void requirePending_이미_처리된_신고는_거부한다() {
        // given
        Report report = savedReport();
        report.moderate(
            ReportActionType.APPROVE, ReportActionReason.ABUSE, null, ReportTargetRemoval.NONE, "1", "모루",
            Instant.now()
        );

        // when & then
        assertThatThrownBy(report::requirePending)
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_ALREADY_MODERATED)
            );
    }

    @Test
    @DisplayName("사용자 신고는 상세 내용을 trim해 reason에 저장하고 대상 회원 ID와 스냅샷을 보존한다")
    void createUser_사용자_신고를_만든다() {
        // given & when
        Report report = Report.createUser(
            1L, 2L, ReportReason.HARASSMENT, "  반복해서 불쾌한 메시지를 보내요.  ",
            ReportTargetSnapshot.of(2L, null, "대상닉네임")
        );

        // then
        assertThat(report.getReporterId()).isEqualTo(1L);
        assertThat(report.getTargetType()).isEqualTo(ReportTargetType.USER);
        assertThat(report.getTargetId()).isEqualTo(2L);
        assertThat(report.getThreadId()).isNull();
        assertThat(report.getReasonCode()).isEqualTo(ReportReason.HARASSMENT);
        assertThat(report.getReason()).isEqualTo("반복해서 불쾌한 메시지를 보내요.");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getDeliveryStatus()).isEqualTo(ReportDeliveryStatus.PENDING);
        assertThat(report.getTargetSnapshot().getAuthorId()).isEqualTo(2L);
        assertThat(report.getTargetSnapshot().getContent()).isNull();
        assertThat(report.getTargetSnapshot().getContext()).isEqualTo("대상닉네임");
    }

    @Test
    @DisplayName("사용자 신고 상세 내용은 정확히 500자까지 받는다")
    void createUser_500자는_허용한다() {
        // given & when
        Report report = Report.createUser(1L, 2L, ReportReason.ETC, "가".repeat(500), null);

        // then
        assertThat(report.getReason()).hasSize(500);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n\t "})
    @DisplayName("사용자 신고는 상세 내용이 없거나 공백뿐이면 거절한다")
    void createUser_상세_내용이_비어_있으면_거절한다(String detail) {
        assertThatThrownBy(() -> Report.createUser(1L, 2L, ReportReason.SPAM, detail, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용자 신고 상세 내용이 501자면 거절한다")
    void createUser_501자는_거절한다() {
        assertThatThrownBy(() -> Report.createUser(1L, 2L, ReportReason.SPAM, "가".repeat(501), null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("신고자와 대상이 같으면 REPORT_SELF_REPORT로 거절한다")
    void createUser_자기_자신_신고를_거절한다() {
        assertThatThrownBy(() -> Report.createUser(1L, 1L, ReportReason.SPAM, "상세", null))
            .isInstanceOfSatisfying(ReportDomainException.class, exception ->
                assertThat(exception.getBaseCode()).isEqualTo(ReportErrorCode.REPORT_SELF_REPORT)
            );
    }

    @ParameterizedTest
    @EnumSource(value = ReportReason.class, names = {"ABUSE", "INAPPROPRIATE", "PRIVACY"})
    @DisplayName("사용자 신고에 쓸 수 없는 사유는 거절한다")
    void createUser_허용되지_않는_사유를_거절한다(ReportReason reason) {
        assertThatThrownBy(() -> Report.createUser(1L, 2L, reason, "상세", null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("대상 종류별 허용 사유를 검증한다")
    void isAllowedFor_대상별_허용_사유() {
        assertThat(allowed(ReportTargetType.USER))
            .containsExactlyInAnyOrder(
                ReportReason.HARASSMENT, ReportReason.IMPERSONATION, ReportReason.SPAM, ReportReason.ETC
            );
        assertThat(allowed(ReportTargetType.THREAD_MESSAGE))
            .containsExactlyInAnyOrder(
                ReportReason.SPAM, ReportReason.ABUSE, ReportReason.INAPPROPRIATE, ReportReason.PRIVACY,
                ReportReason.ETC
            );
    }

    @Test
    @DisplayName("스레드 메시지 신고는 사용자 신고 전용 사유를 거절한다")
    void createThreadMessage_사용자_전용_사유를_거절한다() {
        assertThatThrownBy(() -> Report.createThreadMessage(1L, 2L, 3L, ReportReason.HARASSMENT, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용자 신고는 처리해도 대상을 삭제하지 않는다")
    void removesTargetOn_사용자_신고는_삭제하지_않는다() {
        // given
        Report report = Report.createUser(1L, 2L, ReportReason.IMPERSONATION, "상세", null);

        // when & then
        assertThat(report.removesTargetOn(ReportActionType.APPROVE)).isFalse();
        assertThat(report.removesTargetOn(ReportActionType.REJECT)).isFalse();
    }

    @Test
    @DisplayName("기존 create로는 사용자 신고를 만들 수 없다")
    void create_사용자_신고는_createUser를_쓰게_한다() {
        assertThatThrownBy(() -> Report.create(1L, ReportTargetType.USER, 2L, "상세"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용자 신고 전용 처리 사유는 처리(APPROVE)에만 쓴다")
    void reportActionReason_괴롭힘과_사칭은_처리_사유다() {
        assertThat(ReportActionReason.HARASSMENT.actionType()).isEqualTo(ReportActionType.APPROVE);
        assertThat(ReportActionReason.IMPERSONATION.actionType()).isEqualTo(ReportActionType.APPROVE);
        assertThat(ReportActionReason.HARASSMENT.isMemoRequired()).isFalse();
        assertThat(ReportActionReason.IMPERSONATION.isMemoRequired()).isFalse();
    }

    private static List<ReportReason> allowed(ReportTargetType targetType) {
        return Arrays.stream(ReportReason.values())
            .filter(reason -> reason.isAllowedFor(targetType))
            .toList();
    }

    private Report savedReport() {
        Report report = Report.create(1L, ReportTargetType.POST, 2L, null);
        ReflectionTestUtils.setField(report, "id", 901L);
        return report;
    }
}
