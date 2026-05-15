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
 * N_fe, N_be 는 각각 {5, 6} 중 무작위. 프론트엔드는 {WEB, ANDROID, IOS}, 백엔드는
 * {NODEJS, SPRINGBOOT} 중 균등 무작위로 결정한다.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class PartAssignmentPolicy {

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
     * 한 프로젝트에 채울 슬롯 파트 리스트를 만든다. 첫 원소는 항상 PLAN.
     * 호출자는 슬롯 순서대로 같은 수의 Member 를 매칭해서 add 한다.
     */
    public List<ChallengerPart> nextProjectSlots() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int feSize = 5 + random.nextInt(2);
        int beSize = 5 + random.nextInt(2);
        List<ChallengerPart> slots = new ArrayList<>(1 + feSize + beSize);
        slots.add(ChallengerPart.PLAN);
        for (int i = 0; i < feSize; i++) {
            slots.add(FRONTEND_PARTS.get(random.nextInt(FRONTEND_PARTS.size())));
        }
        for (int i = 0; i < beSize; i++) {
            slots.add(BACKEND_PARTS.get(random.nextInt(BACKEND_PARTS.size())));
        }
        return slots;
    }
}
