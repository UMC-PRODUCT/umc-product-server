package com.umc.product.terms.domain;


import com.umc.product.common.BaseEntity;
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
@Table(name = "terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermsType type;  // SERVICE, PRIVACY, MARKETING, EVENT, etc.

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String version;  // "1.0", "2.0", etc.

    @Column(nullable = false)
    private boolean required;  // 필수 동의 여부

    @Column(nullable = false)
    private boolean active;  // 현재 활성화된 약관인지

    @Column(nullable = false)
    private Instant effectiveDate;  // 시행일

    @Builder
    private Terms(TermsType type, String title, String content,
                  String version, boolean required, Instant effectiveDate) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.version = version;
        this.required = required;
        this.active = true;
        this.effectiveDate = effectiveDate;
    }

    public void deactivate() {
        this.active = false;
    }
}
