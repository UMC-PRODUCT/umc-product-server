package com.umc.product.inquiry.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.inquiry.domain.enums.InquiryMessageSenderType;
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
@Table(name = "inquiry_message")
public class InquiryMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "sender_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private InquiryMessageSenderType senderType;

    @Column(name = "sender_challenger_id", nullable = false)
    private Long senderChallengerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static InquiryMessage create(
        Long inquiryId,
        InquiryMessageSenderType senderType,
        Long senderChallengerId,
        String content
    ) {
        return InquiryMessage.builder()
            .inquiryId(inquiryId)
            .senderType(senderType)
            .senderChallengerId(senderChallengerId)
            .content(content)
            .build();
    }
}
