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
@Table(name = "umc_product_generation")
public class UmcProductGeneration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long generation;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder(access = AccessLevel.PRIVATE)
    private UmcProductGeneration(Long generation, Instant startAt, Instant endAt, boolean isActive) {
        validate(generation, startAt, endAt);
        this.generation = generation;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isActive = isActive;
    }

    public static UmcProductGeneration create(Long generation, Instant startAt, Instant endAt, boolean isActive) {
        return UmcProductGeneration.builder()
            .generation(generation)
            .startAt(startAt)
            .endAt(endAt)
            .isActive(isActive)
            .build();
    }

    public void update(Long generation, Instant startAt, Instant endAt, Boolean isActive) {
        Long nextGeneration = generation != null ? generation : this.generation;
        Instant nextStartAt = startAt != null ? startAt : this.startAt;
        Instant nextEndAt = endAt != null ? endAt : this.endAt;
        validate(nextGeneration, nextStartAt, nextEndAt);
        this.generation = nextGeneration;
        this.startAt = nextStartAt;
        this.endAt = nextEndAt;
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public void active() {
        this.isActive = true;
    }

    public void inactive() {
        this.isActive = false;
    }

    private static void validate(Long generation, Instant startAt, Instant endAt) {
        if (generation == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_REQUIRED);
        }
        if (startAt == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_START_AT_REQUIRED);
        }
        if (endAt == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_END_AT_REQUIRED);
        }
        if (!startAt.isBefore(endAt)) {
            throw new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_PERIOD_INVALID);
        }
    }
}
