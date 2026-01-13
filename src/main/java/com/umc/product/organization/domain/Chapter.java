package com.umc.product.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gisu_id")
    private Gisu gisu;

    private String name;

    @Builder
    private Chapter(Gisu gisu, String name) {
        validate(gisu, name);
        this.gisu = gisu;
        this.name = name;
    }

    private static void validate(Gisu gisu, String name) {
        if (gisu == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_REQUIRED);
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.ORGAN_NAME_REQUIRED);
        }
    }
}
