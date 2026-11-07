package com.umc.product.test.domain.roster;

public record SchoolCode(String value) {
    public SchoolCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("학교 코드는 비어 있을 수 없습니다.");
        }
    }

    public static SchoolCode of(String value) {
        return new SchoolCode(value);
    }
}
