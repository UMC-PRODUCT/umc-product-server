package com.umc.product.term.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.term.domain.enums.TermType;
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
@Table(name = "terms_consent")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermConsent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;  // ID 참조만

    @Enumerated(EnumType.STRING)
    @Column(name = "term_type", nullable = false, length = 20)
    private TermType termType;

    @Column(name = "agreed_at", nullable = false)
    private Instant agreedAt;

    @Builder
    private TermConsent(Long memberId, TermType termType, Instant agreedAt) {
        this.memberId = memberId;
        this.termType = termType;
        this.agreedAt = agreedAt != null ? agreedAt : Instant.now();
    }
}
