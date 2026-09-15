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

    /**
     * 이 파트 위에 infra(부가 트랙)를 얹을 수 있는지 여부. 개발 파트(웹/모바일 프로덕트 엔지니어)만 가능하다.
     */
    public boolean canHaveInfra() {
        return this == WEB_PRODUCT_ENGINEER || this == MOBILE_PRODUCT_ENGINEER;
    }

    /**
     * 챌린저의 기본 파트로 지정할 수 있는지 여부. INFRA는 커리큘럼과 스터디그룹에서만 독립 파트로 사용한다.
     */
    public boolean canBeAssignedToChallenger() {
        return this != INFRA;
    }

    /**
     * 이 파트로 개설된 커리큘럼/스터디를 주어진 챌린저(단일 파트 + infra 여부)가 수강하는지 여부.
     * INFRA 커리큘럼은 infra를 수강하는 개발 파트 챌린저가 대상이고, 그 외에는 파트가 일치해야 한다.
     */
    public boolean coversChallenger(ChallengerPart challengerPart, boolean challengerInfra) {
        if (this == INFRA) {
            return challengerInfra;
        }
        return this == challengerPart;
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
