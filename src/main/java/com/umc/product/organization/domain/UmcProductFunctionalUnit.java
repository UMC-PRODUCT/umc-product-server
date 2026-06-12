package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "umc_product_functional_unit",
    indexes = {
        @Index(
            name = "ix_umc_product_functional_unit_generation_type",
            columnList = "umc_product_generation_id, type"
        ),
        @Index(name = "ix_umc_product_functional_unit_parent", columnList = "parent_unit_id")
    }
)
public class UmcProductFunctionalUnit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "umc_product_generation_id", nullable = false)
    private Long umcProductGenerationId;

    @Column(name = "parent_unit_id")
    private Long parentUnitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UmcProductFunctionalUnitType type;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder(access = AccessLevel.PRIVATE)
    private UmcProductFunctionalUnit(
        Long umcProductGenerationId,
        Long parentUnitId,
        UmcProductFunctionalUnitType type,
        String code,
        String name,
        String description,
        int sortOrder,
        boolean isActive
    ) {
        validate(umcProductGenerationId, type, code, name);
        this.umcProductGenerationId = umcProductGenerationId;
        this.parentUnitId = parentUnitId;
        this.type = type;
        this.code = normalizeRequired(code);
        this.name = normalizeRequired(name);
        this.description = normalizeNullable(description);
        this.sortOrder = sortOrder;
        this.isActive = isActive;
    }

    public static UmcProductFunctionalUnit create(
        Long umcProductGenerationId,
        Long parentUnitId,
        UmcProductFunctionalUnitType type,
        String code,
        String name,
        String description,
        int sortOrder,
        boolean isActive
    ) {
        return UmcProductFunctionalUnit.builder()
            .umcProductGenerationId(umcProductGenerationId)
            .parentUnitId(parentUnitId)
            .type(type)
            .code(code)
            .name(name)
            .description(description)
            .sortOrder(sortOrder)
            .isActive(isActive)
            .build();
    }

    public void update(
        Long parentUnitId,
        UmcProductFunctionalUnitType type,
        String code,
        String name,
        String description,
        Integer sortOrder,
        Boolean isActive
    ) {
        UmcProductFunctionalUnitType nextType = type != null ? type : this.type;
        String nextCode = code != null ? code : this.code;
        String nextName = name != null ? name : this.name;
        validate(this.umcProductGenerationId, nextType, nextCode, nextName);
        this.parentUnitId = parentUnitId;
        this.type = nextType;
        this.code = normalizeRequired(nextCode);
        this.name = normalizeRequired(nextName);
        this.description = normalizeNullable(description);
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    private static void validate(
        Long umcProductGenerationId,
        UmcProductFunctionalUnitType type,
        String code,
        String name
    ) {
        if (umcProductGenerationId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_REQUIRED);
        }
        if (type == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_TYPE_REQUIRED);
        }
        if (code == null || code.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_CODE_REQUIRED);
        }
        if (name == null || name.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_NAME_REQUIRED);
        }
    }

    private static String normalizeRequired(String value) {
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
