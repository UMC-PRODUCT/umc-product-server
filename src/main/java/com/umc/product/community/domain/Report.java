package com.umc.product.community.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.community.domain.enums.ReportStatus;
import com.umc.product.community.domain.enums.ReportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId;  // 신고자 챌린저 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;  // POST, COMMENT

    @Column(nullable = false)
    private Long targetId;  // 게시글 ID 또는 댓글 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;  // PENDING, APPROVED, REJECTED

    @Column(length = 500)
    private String reason;  // 선택적 신고 사유

    @Builder
    private Report(Long reporterId, ReportTargetType targetType, Long targetId, String reason) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.status = ReportStatus.PENDING;
        this.reason = reason;
    }

    /**
     * 신고 생성 팩토리 메서드
     * @param reporterId 신고자 챌린저 ID
     * @param targetType 신고 대상 타입 (POST, COMMENT)
     * @param targetId 신고 대상 ID
     * @param reason 신고 사유 (선택)
     */
    public static Report create(Long reporterId, ReportTargetType targetType, Long targetId, String reason) {
        return Report.builder()
                .reporterId(reporterId)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .build();
    }

    // 신고 상태 변경 (관리자용)
    public void approve() {
        this.status = ReportStatus.APPROVED;
    }

    public void reject() {
        this.status = ReportStatus.REJECTED;
    }
}
