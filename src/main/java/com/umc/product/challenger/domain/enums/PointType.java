package com.umc.product.challenger.domain.enums;

public enum PointType {
    BEST_WORKBOOK,
    WARNING,
    OUT;

    public static PointType random() {
        return values()[(int) (Math.random() * values().length)];
    }
}
