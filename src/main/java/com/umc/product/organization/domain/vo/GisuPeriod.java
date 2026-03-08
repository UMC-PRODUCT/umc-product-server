package com.umc.product.organization.domain.vo;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GisuPeriod {

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    private GisuPeriod(Instant startAt, Instant endAt) {
        if (startAt == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_START_AT_REQUIRED);
        }
        if (endAt == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_END_AT_REQUIRED);
        }
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_PERIOD_INVALID);
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static GisuPeriod of(Instant startAt, Instant endAt) {
        return new GisuPeriod(startAt, endAt);
    }
}
