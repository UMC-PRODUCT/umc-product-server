package com.umc.product.inquiry.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.inquiry.domain.enums.InquiryCategory;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTarget;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
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

    // 운영진 중 누군가 한 명이라도 열람했는지 여부. 채팅방 단위로 읽음 상태 관리
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    /**
     * 문의사항을 생성한다. 초기 status는 RECEIVED, isRead는 false로 고정된다.
     */
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
            .isRead(false)
            .build();
    }

    /**
     * 운영진이 처음 문의를 열람할 때 호출된다. 이미 true이면 아무 작업도 하지 않는다
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
        }
    }

    /**
     * RECEIVED → IN_PROGRESS 전환. RECEIVED 상태에서만 호출 가능하다.
     */
    public void startProgress() {
        if (this.status != InquiryStatus.RECEIVED) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_INVALID_STATUS_FOR_PROGRESS);
        }
        this.status = InquiryStatus.IN_PROGRESS;
    }

    /**
     * RECEIVED 또는 IN_PROGRESS → CLOSED 전환. 이미 CLOSED이면 예외를 던진다.
     */
    public void close() {
        if (this.status == InquiryStatus.CLOSED) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ALREADY_CLOSED);
        }
        this.status = InquiryStatus.CLOSED;
    }

    /**
     * CLOSED → IN_PROGRESS 전환. 문의자 또는 운영진이 메시지를 전송할 때 자동으로 호출된다.
     */
    public void reopen() {
        if (this.status != InquiryStatus.CLOSED) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_INVALID_STATUS_FOR_REOPEN);
        }
        this.status = InquiryStatus.IN_PROGRESS;
    }
}
