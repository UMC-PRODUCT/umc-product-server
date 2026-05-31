package com.umc.product.term.domain;

import java.time.Instant;

import com.umc.product.common.BaseEntity;
import com.umc.product.term.domain.enums.TermConsentStatus;
import com.umc.product.term.domain.enums.TermType;

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
@Table(name = "term_consent_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermConsentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 20)
    private TermType termType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TermConsentStatus status; // AGREED, WITHDRAWN

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Builder
    private TermConsentLog(Long memberId, Long termId, TermType termType, TermConsentStatus status) {
        this.memberId = memberId;
        this.termId = termId;
        this.termType = termType;
        this.status = status;
        this.occurredAt = Instant.now();
    }
}
