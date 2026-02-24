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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermType type;  // SERVICE, PRIVACY, MARKETING, EVENT, etc.

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private boolean required;  // 필수 동의 여부

    @Column(nullable = false)
    private boolean active;  // 현재 활성화된 약관인지

    @Builder
    private Term(TermType type, String link, boolean required) {
        this.type = type;
        this.link = link;
        this.required = required;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
