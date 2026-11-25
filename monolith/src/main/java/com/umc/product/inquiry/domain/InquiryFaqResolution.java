package com.umc.product.inquiry.domain;

import java.util.Objects;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원이 대표 질문에서 "해결됐어요"를 누른 기록. 회원마다 대표 질문 하나에 한 번만 센다.
 */
@Entity
@Table(name = "inquiry_faq_resolution")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryFaqResolution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faq_id", nullable = false)
    private Long faqId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public static InquiryFaqResolution create(Long faqId, Long memberId) {
        InquiryFaqResolution resolution = new InquiryFaqResolution();
        resolution.faqId = Objects.requireNonNull(faqId);
        resolution.memberId = Objects.requireNonNull(memberId);
        return resolution;
    }
}
