package com.umc.product.challenger.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointType {
    BEST_WORKBOOK(-0.5),
    WARNING(0.0),
    OUT(1.0);

    private final double value;

    public static PointType random() {
        return values()[(int) (Math.random() * values().length)];
    }
}
