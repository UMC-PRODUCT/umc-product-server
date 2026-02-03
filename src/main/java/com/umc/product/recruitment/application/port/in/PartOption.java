package com.umc.product.recruitment.application.port.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartOption {
    ALL("ALL", "전체"),
    PLAN("PLAN", "Plan"),
    DESIGN("DESIGN", "Design"),
    WEB("WEB", "Web"),
    IOS("IOS", "iOS"),
    ANDROID("ANDROID", "Android"),
    SPRINGBOOT("SPRINGBOOT", "SpringBoot"),
    NODEJS("NODEJS", "Node.js");

    private final String code;
    private final String label;

}
