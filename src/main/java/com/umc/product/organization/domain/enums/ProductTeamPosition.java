package com.umc.product.organization.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductTeamPosition {
    UNSPECIFIED("미지정", 0),
    PRODUCT_OWNER("Product Owner", 1),
    FRONTEND_DEVELOPER("Frontend Developer", 2),
    BACKEND_DEVELOPER("Backend Developer", 3),
    PRODUCT_DESIGNER("Product Designer", 4),
    IOS_DEVELOPER("iOS Developer", 5),
    ANDROID_DEVELOPER("Android Developer", 6),
    ETC("기타", 99);

    private final String displayName;
    private final int sortOrder;
}
