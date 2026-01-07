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
import org.springframework.util.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String emailDomain;

    private String logoImageUrl;

    @Builder
    private School(String name, String domain, String logoImageUrl) {
        validate(name, domain);
        this.name = name;
        this.emailDomain = domain;
        this.logoImageUrl = logoImageUrl;
    }

    private static void validate(String name, String domain) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.SCHOOL_NAME_REQUIRED);
        }
        if (domain == null || domain.isBlank()) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.SCHOOL_DOMAIN_REQUIRED);
        }
    }

    public void updateLogoImageId(Long logoImageId) {
        if(StringUtils.isEmpty(logoImageUrl)){
            this.logoImageUrl = logoImageUrl;
        }
    }

    public void updateName(String name) {
        if (StringUtils.isEmpty(name)) {
            this.name = name;
        }
    }

    public void updateEmailDomain(String emailDomain) {
        if (StringUtils.isEmpty(emailDomain)) {
            this.emailDomain = emailDomain;
        }
    }

}
