package com.umc.product.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gisu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long number;

    private boolean isActive;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Builder
    private Gisu(Long number, boolean isActive, LocalDateTime startAt, LocalDateTime endAt) {
        validate(startAt, endAt);
        this.number = number;
        this.isActive = isActive;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    private static void validate(LocalDateTime startAt, LocalDateTime endAt) {
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

    public boolean isInPeriod(LocalDateTime now) {
        return (now.isEqual(startAt) || now.isAfter(startAt)) && now.isBefore(endAt);
    }
}
