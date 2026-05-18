package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "form_section")
public class FormSection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "order_no")
    private Long orderNo;

    public static FormSection create(Form form, String title, String description, Long orderNo) {
        return FormSection.builder()
            .form(form)
            .title(title)
            .description(description)
            .orderNo(orderNo)
            .build();
    }

    /**
     * 섹션 메타데이터 부분 업데이트.
     * null 인 필드는 기존 값 유지.
     */
    public void update(String title, String description) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
    }

    public void updateOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }
}
