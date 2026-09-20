package com.umc.product.challenger.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("Challenger part와 infra JPA 통합 테스트")
class ChallengerPartPersistenceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    ChallengerJpaRepository repository;

    @Test
    @DisplayName("단일 신규 파트와 인프라 여부를 저장하고 조회한다")
    void 단일_신규_파트와_인프라_여부를_저장하고_조회한다() {
        Challenger saved = repository.saveAndFlush(Challenger.builder()
            .memberId(88001L)
            .part(ChallengerPart.WEB_PRODUCT_ENGINEER)
            .infra(true)
            .gisuId(99001L)
            .build());
        entityManager.clear();

        Challenger found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(found.isInfra()).isTrue();
    }

    @Test
    @DisplayName("레거시 파트는 그대로 저장하고 인프라 여부는 false를 유지한다")
    void 레거시_파트는_그대로_저장한다() {
        Challenger saved = repository.saveAndFlush(Challenger.builder()
            .memberId(88002L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(99001L)
            .build());
        entityManager.clear();

        Challenger found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getPart()).isEqualTo(ChallengerPart.SPRINGBOOT);
        assertThat(found.isInfra()).isFalse();
    }
}
