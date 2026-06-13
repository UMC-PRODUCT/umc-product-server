package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "umc_product_functional_membership",
    indexes = {
        @Index(
            name = "ix_umc_product_functional_membership_generation_unit",
            columnList = "umc_product_generation_id, functional_unit_id"
        ),
        @Index(name = "ix_umc_product_functional_membership_member", columnList = "umc_product_member_id")
    }
)
public class UmcProductFunctionalMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umc_product_member_id", nullable = false)
    private UmcProductMember umcProductMember;

    @Column(name = "umc_product_generation_id", nullable = false)
    private Long umcProductGenerationId;

    @Column(name = "functional_unit_id", nullable = false)
    private Long functionalUnitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UmcProductFunctionalRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UmcProductPosition position;

    @Column(name = "responsibility_title", length = 200)
    private String responsibilityTitle;

    @Column(name = "responsibility_description", length = 1000)
    private String responsibilityDescription;

    @Builder(access = AccessLevel.PRIVATE)
    private UmcProductFunctionalMembership(
        UmcProductMember umcProductMember,
        Long umcProductGenerationId,
        Long functionalUnitId,
        UmcProductFunctionalRole role,
        UmcProductPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        validate(umcProductMember, umcProductGenerationId, functionalUnitId, role, position);
        this.umcProductMember = umcProductMember;
        this.umcProductGenerationId = umcProductGenerationId;
        this.functionalUnitId = functionalUnitId;
        this.role = role;
        this.position = position;
        this.responsibilityTitle = normalizeNullable(responsibilityTitle);
        this.responsibilityDescription = normalizeNullable(responsibilityDescription);
    }

    public static UmcProductFunctionalMembership create(
        UmcProductMember umcProductMember,
        Long umcProductGenerationId,
        Long functionalUnitId,
        UmcProductFunctionalRole role,
        UmcProductPosition position,
        String responsibilityTitle,
        String responsibilityDescription
    ) {
        return UmcProductFunctionalMembership.builder()
            .umcProductMember(umcProductMember)
            .umcProductGenerationId(umcProductGenerationId)
            .functionalUnitId(functionalUnitId)
            .role(role)
            .position(position)
            .responsibilityTitle(responsibilityTitle)
            .responsibilityDescription(responsibilityDescription)
            .build();
    }

    private static void validate(
        UmcProductMember umcProductMember,
        Long umcProductGenerationId,
        Long functionalUnitId,
        UmcProductFunctionalRole role,
        UmcProductPosition position
    ) {
        if (umcProductMember == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_MEMBER_REQUIRED);
        }
        if (umcProductGenerationId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_REQUIRED);
        }
        if (functionalUnitId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_REQUIRED);
        }
        if (role == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_ROLE_REQUIRED);
        }
        if (position == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_POSITION_REQUIRED);
        }
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
