package com.umc.product.inquiry.domain;

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
 * 대표 질문이 누구에게 해당하는지. 관리 API 없이 SQL로 추가·비활성화한다.
 */
@Entity
@Table(name = "inquiry_faq_audience")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryFaqAudience extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    public static InquiryFaqAudience create(String name, int displayOrder) {
        InquiryFaqAudience audience = new InquiryFaqAudience();
        audience.name = name;
        audience.displayOrder = displayOrder;
        audience.active = true;
        return audience;
    }
}
