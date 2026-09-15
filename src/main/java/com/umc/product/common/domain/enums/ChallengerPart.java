package com.umc.product.common.domain.enums;

import java.util.Arrays;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerPart {
    // 공통(구·신 기수 모두 사용)
    PLAN("기획", 0),
    DESIGN("디자인", 1),
    // 레거시(구 기수 데이터 보존용, 신규 발급 안 함)
    WEB("웹", 2),
    ANDROID("안드로이드", 3),
    IOS("iOS", 4),
    NODEJS("노드", 5),
    SPRINGBOOT("스프링부트", 6),
    ADMIN("운영진", 7),
    // 신규(개편 파트)
    WEB_PRODUCT_ENGINEER("웹 프로덕트 엔지니어", 8),
    MOBILE_PRODUCT_ENGINEER("모바일 프로덕트 엔지니어", 9),
    INFRA("인프라", 10),
    ;

    private final String displayName;
    private final int sortOrder;

    public static ChallengerPart from(String part) {
        if (part == null || part.isBlank()) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND);
        }
        return Arrays.stream(ChallengerPart.values())
            .filter(challengerPart -> challengerPart.name().equals(part))
            .findFirst()
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND));


    }
}
