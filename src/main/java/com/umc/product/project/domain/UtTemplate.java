package com.umc.product.project.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.project.domain.enums.UtContext;
import com.umc.product.project.domain.enums.UtTargetType;
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
@Table(name = "ut_template")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UtTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UtContext context;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UtTargetType targetType;

    @Column(nullable = false)
    private Long formId; // Survey Form ID 참조

    @Column(nullable = false)
    private boolean isActive;

    public static UtTemplate create(UtContext context, UtTargetType targetType, Long formId) {
        return UtTemplate.builder()
            .context(context)
            .targetType(targetType)
            .formId(formId)
            .isActive(true)
            .build();
    }
}
