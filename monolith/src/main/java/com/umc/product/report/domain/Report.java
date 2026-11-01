package com.umc.product.report.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.umc.product.common.BaseEntity;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetRemoval;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    /** 사용자 신고 상세 내용의 최대 길이. {@code reason} 컬럼 길이와 같습니다. */
    public static final int DETAIL_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신고를 생성한 회원의 {@code member.id}. */
    @Column(nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(length = 500)
    private String reason;

    @Column(name = "thread_id")
    private Long threadId;

    /** 대표 신고 사유(첫 번째로 고른 사유). 배포 중인 예전 서버가 이 컬럼만 읽으므로 계속 채웁니다. */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 32)
    private ReportReason reasonCode;

    /** 신고자가 고른 신고 사유 전체. 고른 순서를 유지하며 중복이 없습니다. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_reason", joinColumns = @JoinColumn(name = "report_id"))
    @OrderColumn(name = "sort_order")
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 32, nullable = false)
    @BatchSize(size = 100)
    private List<ReportReason> reasons = new ArrayList<>();

    /** 운영 채널 전달 상태. 전달 기능 이전에 접수된 신고는 {@code null}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 20)
    private ReportDeliveryStatus deliveryStatus;

    /** 전달에 실패한 횟수. 성공한 시도는 세지 않습니다. */
    @Column(name = "delivery_attempt_count", nullable = false)
    private int deliveryAttemptCount;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Embedded
    private ReportTargetSnapshot targetSnapshot;

    @Builder
    private Report(
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        Long threadId,
        List<ReportReason> reasons,
        ReportTargetSnapshot targetSnapshot
    ) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = ReportStatus.PENDING;
        this.reason = reason;
        this.threadId = threadId;
        if (reasons != null && !reasons.isEmpty()) {
            this.reasons = new ArrayList<>(reasons);
            this.reasonCode = reasons.get(0);
        }
        this.deliveryStatus = ReportDeliveryStatus.PENDING;
        this.deliveryAttemptCount = 0;
        this.targetSnapshot = targetSnapshot;
    }

    public static Report create(Long reporterId, ReportTargetType targetType, Long targetId, String reason) {
        if (targetType == ReportTargetType.THREAD_MESSAGE) {
            throw new IllegalArgumentException("THREAD_MESSAGE reports require threadId and reasonCode");
        }
        if (targetType == ReportTargetType.USER) {
            throw new IllegalArgumentException("USER reports require reasonCode and detail");
        }
        return Report.builder()
            .reporterId(reporterId)
            .targetType(targetType)
            .targetId(targetId)
            .reason(reason)
            .build();
    }

    public static Report createThreadMessage(
        Long reporterId,
        Long threadId,
        Long messageId,
        List<ReportReason> reasons,
        ReportTargetSnapshot targetSnapshot
    ) {
        return Report.builder()
            .reporterId(requirePositive(reporterId, "reporterId"))
            .targetType(ReportTargetType.THREAD_MESSAGE)
            .targetId(requirePositive(messageId, "messageId"))
            .threadId(requirePositive(threadId, "threadId"))
            .reasons(requireReasonsFor(ReportTargetType.THREAD_MESSAGE, reasons))
            .targetSnapshot(targetSnapshot)
            .build();
    }

    /**
     * 사용자 신고를 만듭니다. 사유는 고른 순서를 유지하며 중복을 제거합니다. 상세 내용은 앞뒤 공백을 지운 뒤 {@code reason} 컬럼에 저장합니다.
     *
     * @throws ReportDomainException 신고자와 대상이 같으면 {@link ReportErrorCode#REPORT_SELF_REPORT}
     * @throws IllegalArgumentException 사유가 비었거나 사용자 신고에 쓸 수 없는 사유이거나, 상세 내용이 비었거나 500자를 넘을 때
     */
    public static Report createUser(
        Long reporterId,
        Long targetMemberId,
        List<ReportReason> reasons,
        String detail,
        ReportTargetSnapshot targetSnapshot
    ) {
        requirePositive(reporterId, "reporterId");
        requirePositive(targetMemberId, "targetMemberId");
        if (reporterId.equals(targetMemberId)) {
            throw new ReportDomainException(ReportErrorCode.REPORT_SELF_REPORT);
        }
        return Report.builder()
            .reporterId(reporterId)
            .targetType(ReportTargetType.USER)
            .targetId(targetMemberId)
            .reason(requireDetail(detail))
            .reasons(requireReasonsFor(ReportTargetType.USER, reasons))
            .targetSnapshot(targetSnapshot)
            .build();
    }

    /**
     * 신고 사유 전체를 고른 순서대로 돌려줍니다. {@code reasons}가 비어 있으면(예전 서버가 접수했거나 backfill 이전의 신고)
     * 대표 사유({@code reasonCode}) 하나로 대체하고, 그것도 없으면 빈 목록을 돌려줍니다.
     */
    public List<ReportReason> getReasonCodes() {
        if (!this.reasons.isEmpty()) {
            return List.copyOf(this.reasons);
        }
        return this.reasonCode == null ? List.of() : List.of(this.reasonCode);
    }

    /**
     * 운영 채널 전달을 아직 시도해야 하는 신고인지 확인합니다. 전달 기능 이전 신고({@code null})는 대상이 아닙니다.
     */
    public boolean isDeliveryPending() {
        return this.deliveryStatus == ReportDeliveryStatus.PENDING;
    }

    public void markDelivered(Instant deliveredAt) {
        this.deliveryStatus = ReportDeliveryStatus.SENT;
        this.deliveredAt = deliveredAt;
    }

    /**
     * 전달 실패를 기록합니다. 시도 횟수가 {@code maxAttempts}에 닿으면 더 이상 자동으로 재시도하지 않도록 {@code FAILED}로 바꿉니다.
     */
    public void recordDeliveryFailure(int maxAttempts) {
        this.deliveryAttemptCount++;
        if (this.deliveryAttemptCount >= maxAttempts) {
            this.deliveryStatus = ReportDeliveryStatus.FAILED;
        }
    }

    /**
     * 전달 대기(PENDING)나 실패(FAILED) 신고를 다시 전달하도록 되돌립니다. 실패 횟수도 처음부터 다시 셉니다.
     *
     * @throws IllegalStateException 이미 전달했거나 전달 기능 이전에 접수된 신고
     */
    public void requeueDelivery() {
        if (this.deliveryStatus != ReportDeliveryStatus.PENDING && this.deliveryStatus != ReportDeliveryStatus.FAILED) {
            throw new IllegalStateException("only PENDING or FAILED reports can be requeued: " + this.deliveryStatus);
        }
        this.deliveryStatus = ReportDeliveryStatus.PENDING;
        this.deliveryAttemptCount = 0;
    }

    /**
     * 처리 대기(PENDING) 상태인지 확인합니다. 대상 콘텐츠를 삭제하기 전에 먼저 호출해, 이미 처리된 신고로 삭제하지 않게 합니다.
     *
     * @throws ReportDomainException 이미 처리되었거나 반려된 신고면 {@link ReportErrorCode#REPORT_ALREADY_MODERATED}
     */
    public void requirePending() {
        if (this.status != ReportStatus.PENDING) {
            throw new ReportDomainException(ReportErrorCode.REPORT_ALREADY_MODERATED);
        }
    }

    /**
     * 해당 조치가 대상 콘텐츠 삭제를 동반하는지 확인합니다. 스레드 메시지 신고를 처리(APPROVE)할 때만 삭제합니다.
     */
    public boolean removesTargetOn(ReportActionType actionType) {
        return actionType == ReportActionType.APPROVE && this.targetType == ReportTargetType.THREAD_MESSAGE;
    }

    /**
     * 처리 대기(PENDING) 신고에 운영자 조치를 한 번만 적용하고, 남길 조치 이력을 돌려줍니다.
     *
     * @throws ReportDomainException 이미 처리되었거나 반려된 신고면 {@link ReportErrorCode#REPORT_ALREADY_MODERATED}
     */
    public ReportAction moderate(
        ReportActionType actionType,
        ReportActionReason reasonCode,
        String memo,
        ReportTargetRemoval targetRemoval,
        String moderatorExternalId,
        String moderatorName,
        Instant actedAt
    ) {
        requirePending();
        ReportAction action = ReportAction.record(
            this.id,
            actionType,
            reasonCode,
            memo,
            targetRemoval,
            moderatorExternalId,
            moderatorName,
            actedAt
        );
        this.status = actionType.resultStatus();
        return action;
    }

    private static Long requirePositive(Long value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static List<ReportReason> requireReasonsFor(ReportTargetType targetType, List<ReportReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("reasons must not be empty");
        }
        List<ReportReason> distinct = List.copyOf(new LinkedHashSet<>(reasons));
        for (ReportReason reason : distinct) {
            if (!reason.isAllowedFor(targetType)) {
                throw new IllegalArgumentException(
                    "reason " + reason + " is not allowed for " + targetType + " reports");
            }
        }
        return distinct;
    }

    private static String requireDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            throw new IllegalArgumentException("detail must not be blank");
        }
        String trimmed = detail.strip();
        if (trimmed.length() > DETAIL_MAX_LENGTH) {
            throw new IllegalArgumentException("detail must not exceed " + DETAIL_MAX_LENGTH + " characters");
        }
        return trimmed;
    }
}
