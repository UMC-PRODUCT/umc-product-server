package com.umc.product.test.application.service;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 프로젝트 시딩의 멤버 슬롯 파트 분배 정책. ADR-017 참조.
 * <p>
 * 한 프로젝트당 PLAN ×1 + 프론트엔드 ×N_fe + 백엔드 ×N_be 의 슬롯을 만든다.
 * N_fe, N_be ∈ {5, 6} 이며 합계는 11~13 명. 프론트엔드는 {WEB, ANDROID, IOS}, 백엔드는
 * {NODEJS, SPRINGBOOT} 중 균등 무작위로 결정한다.
 * <p>
 * 풀 크기에 적응한다: 풀이 13명 이상이면 fe/be 모두 5~6 무작위, 12명이면 (5,6)/(6,5) 중 무작위,
 * 11명이면 (5,5) 고정. 11명 미만이면 빈 리스트를 반환해 호출자가 skip 하도록 한다. 이렇게 하면
 * 풀=12 같은 경계에서 슬롯 13을 만들어 불필요하게 skip 되는 비결정성이 사라진다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class PartAssignmentPolicy {

    static final int PLAN_SLOT_SIZE = 1;
    static final int FE_MIN = 5;
    static final int FE_MAX = 6;
    static final int BE_MIN = 5;
    static final int BE_MAX = 6;
    static final int MIN_TOTAL = PLAN_SLOT_SIZE + FE_MIN + BE_MIN;

    private static final List<ChallengerPart> FRONTEND_PARTS = List.of(
        ChallengerPart.WEB,
        ChallengerPart.ANDROID,
        ChallengerPart.IOS
    );

    private static final List<ChallengerPart> BACKEND_PARTS = List.of(
        ChallengerPart.NODEJS,
        ChallengerPart.SPRINGBOOT
    );

    /**
     * 한 프로젝트에 채울 슬롯 파트 리스트를 풀 크기에 맞춰 만든다. 풀이 {@link #MIN_TOTAL} 명 미만이면
     * 빈 리스트를 반환한다. 첫 원소가 존재하면 항상 PLAN. 호출자는 슬롯 순서대로 같은 수의 Member 를
     * 매칭해서 add 한다.
     *
     * @param poolSize 사용 가능한 멤버 풀 크기
     * @return 풀에 적합한 슬롯 리스트 (빈 리스트면 풀 부족으로 skip 권장)
     */
    public List<ChallengerPart> nextProjectSlots(int poolSize) {
        if (poolSize < MIN_TOTAL) {
            return List.of();
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int feMaxFit = Math.min(FE_MAX, poolSize - PLAN_SLOT_SIZE - BE_MIN);
        int feSize = randomInclusive(random, FE_MIN, feMaxFit);
        int beMaxFit = Math.min(BE_MAX, poolSize - PLAN_SLOT_SIZE - feSize);
        int beSize = randomInclusive(random, BE_MIN, beMaxFit);

        List<ChallengerPart> slots = new ArrayList<>(PLAN_SLOT_SIZE + feSize + beSize);
        slots.add(ChallengerPart.PLAN);
        for (int i = 0; i < feSize; i++) {
            slots.add(FRONTEND_PARTS.get(random.nextInt(FRONTEND_PARTS.size())));
        }
        for (int i = 0; i < beSize; i++) {
            slots.add(BACKEND_PARTS.get(random.nextInt(BACKEND_PARTS.size())));
        }
        return slots;
    }

    private int randomInclusive(ThreadLocalRandom random, int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }
}
