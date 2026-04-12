package com.umc.product.survey.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
// TODO: Builder는 엔티티 단에서 정팩메에 붙여서 사용하도록 하고, 따로 엔티티 단에 붙이지 않는게 어떤지
@Builder
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
}
