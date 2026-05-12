package com.umc.product.inquiry.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.inquiry.domain.enums.InquiryCategory;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "inquiry")
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private InquiryCategory category;

    @Column(name = "target", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private InquiryTarget target;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @Column(name = "author_challenger_id", nullable = false)
    private Long authorChallengerId;

    public static Inquiry create(
        String title,
        String content,
        InquiryCategory category,
        InquiryTarget target,
        Long authorChallengerId
    ) {
        return Inquiry.builder()
            .title(title)
            .content(content)
            .category(category)
            .target(target)
            .status(InquiryStatus.RECEIVED)
            .authorChallengerId(authorChallengerId)
            .build();
    }

    public void startProgress() {
        this.status = InquiryStatus.IN_PROGRESS;
    }

    public void close() {
        this.status = InquiryStatus.CLOSED;
    }

    public void reopen() {
        this.status = InquiryStatus.RECEIVED;
    }
}
