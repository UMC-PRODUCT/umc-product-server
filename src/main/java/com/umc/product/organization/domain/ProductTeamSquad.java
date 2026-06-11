package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_team_squad")
public class ProductTeamSquad extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductTeamSquad(
        String code,
        String name,
        String description,
        Instant startAt,
        Instant endAt,
        int sortOrder,
        boolean isActive
    ) {
        validate(code, name, startAt, endAt);
        this.code = code.trim();
        this.name = name.trim();
        this.description = normalizeNullable(description);
        this.startAt = startAt;
        this.endAt = endAt;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
    }

    public static ProductTeamSquad create(
        String code,
        String name,
        String description,
        Instant startAt,
        Instant endAt,
        int sortOrder,
        boolean isActive
    ) {
        return ProductTeamSquad.builder()
            .code(code)
            .name(name)
            .description(description)
            .startAt(startAt)
            .endAt(endAt)
            .sortOrder(sortOrder)
            .isActive(isActive)
            .build();
    }

    public void update(
        String code,
        String name,
        String description,
        Instant startAt,
        Instant endAt,
        Integer sortOrder,
        Boolean isActive
    ) {
        String nextCode = code != null ? code : this.code;
        String nextName = name != null ? name : this.name;
        validate(nextCode, nextName, startAt, endAt);
        this.code = nextCode.trim();
        this.name = nextName.trim();
        this.description = normalizeNullable(description);
        this.startAt = startAt;
        this.endAt = endAt;
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    private static void validate(String code, String name, Instant startAt, Instant endAt) {
        if (code == null || code.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_CODE_REQUIRED);
        }
        if (name == null || name.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_NAME_REQUIRED);
        }
        if (startAt != null && endAt != null && !startAt.isBefore(endAt)) {
            throw new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_PERIOD_INVALID);
        }
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
