package com.umc.product.recruitment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartKey {
    COMMON("공통"),
    PLAN("Plan"),
    DESIGN("Design"),
    WEB("Web"),
    IOS("iOS"),
    ANDROID("Android"),
    SPRINGBOOT("SpringBoot"),
    NODEJS("Node.js");

    private final String label;
}
