package com.umc.product.common.domain.enums;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
    ;

    private final String displayName;
    private final int sortOrder;

    /**
     * 이 파트 위에 infra(부가 트랙)를 얹을 수 있는지 여부. 개발 파트(웹/모바일 프로덕트 엔지니어)만 가능하다.
     */
    public boolean canHaveInfra() {
        return this == WEB_PRODUCT_ENGINEER || this == MOBILE_PRODUCT_ENGINEER;
    }

    /**
     * 신규 발급(선택 가능) 파트인지 여부. 레거시 파트(WEB/ANDROID/IOS/NODEJS/SPRINGBOOT/ADMIN)는 false이며, 신규 공지 대상 선택 등
     * "현재 사용하는 파트" 목록의 단일 기준이 된다.
     */
    public boolean isSelectable() {
        return this == PLAN
            || this == DESIGN
            || this == WEB_PRODUCT_ENGINEER
            || this == MOBILE_PRODUCT_ENGINEER;
    }

    /**
     * 선택 가능한 파트를 sortOrder 순으로 반환한다.
     */
    public static List<ChallengerPart> selectableValues() {
        return Arrays.stream(values())
            .filter(ChallengerPart::isSelectable)
            .sorted(Comparator.comparingInt(ChallengerPart::getSortOrder))
            .toList();
    }

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
