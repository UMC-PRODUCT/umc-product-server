package com.umc.product.common.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerPart {
    PLAN("기획"),
    DESIGN("디자인"),
    WEB("웹"),
    IOS("iOS"),
    ANDROID("안드로이드"),
    SPRINGBOOT("스프링부트"),
    NODEJS("노드");

    private final String displayName;
}
