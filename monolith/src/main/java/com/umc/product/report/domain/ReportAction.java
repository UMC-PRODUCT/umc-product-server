package com.umc.product.report.domain;

import java.time.Instant;

import com.umc.product.common.BaseEntity;
import com.umc.product.report.domain.enums.ReportActionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신고에 대한 운영자 조치 이력입니다. 한 번 남긴 이력은 바꾸지 않습니다.
 * <p>
 * 조치자는 운영 채널의 사용자 식별자와 표시 이름만 남기고 회원과 연결하지 않습니다.
 */
@Entity
@Table(name = "report_action")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportAction extends BaseEntity {

    public static final int REASON_MAX_LENGTH = 500;
    static final int MODERATOR_EXTERNAL_ID_MAX_LENGTH = 64;
    static final int MODERATOR_NAME_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 32)
    private ReportActionType actionType;

    @Column(name = "reason", nullable = false, length = REASON_MAX_LENGTH)
    private String reason;

    /** 조치자의 운영 채널 사용자 ID (현재 Discord 사용자 ID). */
    @Column(name = "moderator_external_id", nullable = false, length = MODERATOR_EXTERNAL_ID_MAX_LENGTH)
    private String moderatorExternalId;

    /** 조치 시점의 조치자 표시 이름. */
    @Column(name = "moderator_name", nullable = false, length = MODERATOR_NAME_MAX_LENGTH)
    private String moderatorName;

    @Column(name = "acted_at", nullable = false)
    private Instant actedAt;

    private ReportAction(
        Long reportId,
        ReportActionType actionType,
        String reason,
        String moderatorExternalId,
        String moderatorName,
        Instant actedAt
    ) {
        this.reportId = reportId;
        this.actionType = actionType;
        this.reason = reason;
        this.moderatorExternalId = moderatorExternalId;
        this.moderatorName = moderatorName;
        this.actedAt = actedAt;
    }

    static ReportAction record(
        Long reportId,
        ReportActionType actionType,
        String reason,
        String moderatorExternalId,
        String moderatorName,
        Instant actedAt
    ) {
        if (reportId == null || actionType == null || actedAt == null) {
            throw new IllegalArgumentException("reportId, actionType, actedAt must not be null");
        }
        return new ReportAction(
            reportId,
            actionType,
            requireText(reason, "reason", REASON_MAX_LENGTH),
            requireText(moderatorExternalId, "moderatorExternalId", MODERATOR_EXTERNAL_ID_MAX_LENGTH),
            requireText(moderatorName, "moderatorName", MODERATOR_NAME_MAX_LENGTH),
            actedAt
        );
    }

    private static String requireText(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        String trimmed = value.trim();
        // DB의 VARCHAR 길이와 맞추기 위해 char가 아니라 문자(code point) 수로 셉니다.
        if (trimmed.codePointCount(0, trimmed.length()) > maxLength) {
            throw new IllegalArgumentException(name + " must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }
}
