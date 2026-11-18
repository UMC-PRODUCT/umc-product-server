package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 기수 챌린저의 자진 탈부 또는 타의 탈부 등록 정보입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "challenger_deactivation",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_challenger_deactivation_challenger",
        columnNames = "challenger_id"
    )
)
public class ChallengerDeactivation extends BaseEntity {

    public static final int MAX_IMAGE_COUNT = 5;
    public static final int MAX_LINK_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenger_id", nullable = false)
    private Long challengerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChallengerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ChallengerDeactivationReason reason;

    @Column(name = "reason_detail", length = 100)
    private String reasonDetail;

    @Column(name = "memo", length = 100)
    private String memo;

    @Column(name = "registered_by_member_id", nullable = false)
    private Long registeredByMemberId;

    @Builder(access = AccessLevel.PRIVATE)
    private ChallengerDeactivation(
        Long challengerId,
        ChallengerStatus status,
        ChallengerDeactivationReason reason,
        String reasonDetail,
        String memo,
        Long registeredByMemberId
    ) {
        this.challengerId = challengerId;
        this.status = status;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
        this.memo = memo;
        this.registeredByMemberId = registeredByMemberId;
    }

    public static ChallengerDeactivation create(
        Long challengerId,
        ChallengerStatus status,
        ChallengerDeactivationReason reason,
        String reasonDetail,
        String memo,
        Long registeredByMemberId
    ) {
        validateRequired(challengerId, status, reason, registeredByMemberId);
        validateReasonStatus(status, reason);
        String normalizedReasonDetail = validateAndNormalizeReasonDetail(reason, reasonDetail);

        return ChallengerDeactivation.builder()
            .challengerId(challengerId)
            .status(status)
            .reason(reason)
            .reasonDetail(normalizedReasonDetail)
            .memo(validateAndNormalizeMemo(memo))
            .registeredByMemberId(registeredByMemberId)
            .build();
    }

    private static void validateRequired(
        Long challengerId,
        ChallengerStatus status,
        ChallengerDeactivationReason reason,
        Long registeredByMemberId
    ) {
        if (challengerId == null || status == null || reason == null || registeredByMemberId == null) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "챌린저, 탈부 상태, 탈부 사유, 등록자 정보는 필수입니다."
            );
        }
        if (status != ChallengerStatus.WITHDRAWN && status != ChallengerStatus.EXPELLED) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "탈부 상태는 WITHDRAWN 또는 EXPELLED여야 합니다."
            );
        }
    }

    private static void validateReasonStatus(
        ChallengerStatus status,
        ChallengerDeactivationReason reason
    ) {
        if (reason.getStatus() != status) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "탈부 상태와 탈부 사유가 일치하지 않습니다."
            );
        }
    }

    private static String validateAndNormalizeReasonDetail(
        ChallengerDeactivationReason reason,
        String reasonDetail
    ) {
        String normalized = normalizeOptionalText(reasonDetail);
        if (reason.isOther() && normalized == null) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "기타 사유를 선택한 경우 상세 사유를 입력해주세요."
            );
        }
        if (!reason.isOther() && normalized != null) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "상세 사유는 기타 사유를 선택한 경우에만 입력할 수 있습니다."
            );
        }
        if (normalized != null && normalized.codePointCount(0, normalized.length()) > 100) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "상세 사유 글자 수 제한을 초과했습니다."
            );
        }
        return normalized;
    }

    private static String validateAndNormalizeMemo(String memo) {
        String normalized = normalizeOptionalText(memo);
        if (normalized != null && normalized.codePointCount(0, normalized.length()) > 100) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST,
                "메모 글자 수 제한을 초과했습니다."
            );
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
