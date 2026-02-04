package com.umc.product.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gisu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long generation;

    @Column(name = "is_active")
    private boolean isActive;

    private Instant startAt;

    private Instant endAt;

    private Gisu(Long generation, Instant startAt, Instant endAt, boolean isActive) {
        validate(startAt, endAt);
        this.generation = generation;
        this.isActive = isActive;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static Gisu create(Long generation, Instant startAt, Instant endAt, boolean isActive) {
        return new Gisu(generation, startAt, endAt, isActive);
    }

    private static void validate(Instant startAt, Instant endAt) {
        if (startAt == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_START_AT_REQUIRED);
        }
        if (endAt == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_END_AT_REQUIRED);
        }
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_PERIOD_INVALID);
        }
    }

    public void active() {
        this.isActive = true;
    }

    public void inactive() {
        this.isActive = false;
    }

    public void updateIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
