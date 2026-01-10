package com.umc.product.schedule.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

//인정 위치 범위
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationRange {

    @Column(name = "range_meters")
    private int meters;

    private LocationRange(int meters) {
        if (meters <= 0) {
            throw new IllegalArgumentException("범위는 0보다 커야 합니다");
        }
        if (meters > 10000) {
            throw new IllegalArgumentException("범위는 10,000m를 초과할 수 없습니다");
        }
        this.meters = meters;
    }

    public static LocationRange of(int meters) {
        return new LocationRange(meters);
    }

    public static LocationRange ofKilometers(double kilometers) {
        return new LocationRange((int) (kilometers * 1000));
    }
}
