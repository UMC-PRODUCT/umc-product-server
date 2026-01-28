package com.umc.product.terms.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.terms.domain.enums.TermsConsentStatus;
import com.umc.product.terms.domain.enums.TermsType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terms_consent_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsConsentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 20)
    private TermsType termType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TermsConsentStatus status; // AGREED, WITHDRAWN

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Builder
    private TermsConsentLog(Long memberId, TermsType termType, TermsConsentStatus status) {
        this.memberId = memberId;
        this.termType = termType;
        this.status = status;
        this.occurredAt = Instant.now();
    }
}
