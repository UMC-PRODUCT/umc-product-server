package com.umc.product.common.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerPart {
    PLAN("기획", 0),
    DESIGN("디자인", 1),
    WEB("웹", 2),
    ANDROID("안드로이드", 3),
    IOS("iOS", 4),
    NODEJS("노드", 5),
    SPRINGBOOT("스프링부트", 6);

    private final String displayName;
    private final int sortOrder;

    public static ChallengerPart random() {
        return values()[(int) (Math.random() * values().length)];
    }
}
