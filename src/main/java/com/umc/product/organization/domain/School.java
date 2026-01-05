package com.umc.product.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String emailDomain;

    private Long logoImageId;

    @Builder
    private School(String name, String domain, Long logoImageId) {
        validate(name, domain);
        this.name = name;
        this.emailDomain = domain;
        this.logoImageId = logoImageId;
    }

    private static void validate(String name, String domain) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.SCHOOL_NAME_REQUIRED);
        }
        if (domain == null || domain.isBlank()) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.SCHOOL_DOMAIN_REQUIRED);
        }
    }

}
