package com.umc.product.test.application.service;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시나리오 시딩이 한 프로젝트에 TO 를 거는 파트 묶음.
 * <p>
 * 묶음마다 남은 용량이 있는 파트 하나만 골라 {@code [minQuota, maxQuota]} 범위의 TO 를 건다. 웹·모바일 프로덕트
 * 엔지니어를 한 묶음에 둔 것은 한 프로젝트가 두 파트를 함께 모집할 수 없기 때문이다.
 */
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
enum ScenarioPartGroup {
    DESIGN(1, 2, List.of(ChallengerPart.DESIGN)),
    LEGACY_FRONTEND(3, 4, List.of(ChallengerPart.WEB, ChallengerPart.ANDROID, ChallengerPart.IOS)),
    LEGACY_BACKEND(3, 4, List.of(ChallengerPart.NODEJS, ChallengerPart.SPRINGBOOT)),
    PRODUCT_ENGINEER(3, 4, List.of(ChallengerPart.WEB_PRODUCT_ENGINEER, ChallengerPart.MOBILE_PRODUCT_ENGINEER));

    private final long minQuota;
    private final long maxQuota;
    private final List<ChallengerPart> parts;
}
