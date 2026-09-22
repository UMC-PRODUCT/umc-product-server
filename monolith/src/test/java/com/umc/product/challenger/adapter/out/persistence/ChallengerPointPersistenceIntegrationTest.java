package com.umc.product.challenger.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerPoint;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("ChallengerPoint JPA 통합 테스트")
class ChallengerPointPersistenceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    ChallengerJpaRepository challengerRepository;

    @Autowired
    ChallengerPointJpaRepository pointRepository;

    @Autowired
    ManageChallengerUseCase manageChallengerUseCase;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("기존에 저장된 구형 유형의 개별 배점은 조회 시 변경하지 않는다")
    void 기존에_저장된_구형_유형의_개별_배점은_조회_시_변경하지_않는다() {
        // given
        Challenger challenger = challengerRepository.saveAndFlush(Challenger.builder()
            .memberId(88005L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(99001L)
            .build());
        ChallengerPoint saved = pointRepository.saveAndFlush(
            ChallengerPoint.create(challenger, PointType.BEST_WORKBOOK, "기존 부여 기록")
        );
        // 신규 생성 검증 이전에 저장된 데이터를 재현한다.
        jdbcTemplate.update("UPDATE challenger_point SET point_value = ? WHERE id = ?", 2, saved.getId());
        entityManager.clear();

        // when
        ChallengerPoint found = pointRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getType()).isEqualTo(PointType.BEST_WORKBOOK);
        assertThat(found.getPointValue()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("root 컬렉션 cascade 없이 ChallengerPoint를 저장하고 조회한다")
    void root_컬렉션_cascade_없이_ChallengerPoint를_저장하고_조회한다() {
        Challenger challenger = challengerRepository.saveAndFlush(Challenger.builder()
            .memberId(88003L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(99001L)
            .build());
        ChallengerPoint saved = pointRepository.saveAndFlush(
            ChallengerPoint.create(challenger, PointType.CUSTOM, 4, "기여")
        );
        entityManager.clear();

        ChallengerPoint found = pointRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getChallengerId()).isEqualTo(challenger.getId());
        assertThat(found.getPointValue()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("root 컬렉션 cascade 없이 Challenger와 소속 Point를 함께 삭제한다")
    void root_컬렉션_cascade_없이_Challenger와_소속_Point를_함께_삭제한다() {
        Challenger challenger = challengerRepository.saveAndFlush(Challenger.builder()
            .memberId(88004L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(99001L)
            .build());
        pointRepository.saveAndFlush(ChallengerPoint.create(
            challenger,
            PointType.WARNING,
            "삭제 회귀"
        ));

        manageChallengerUseCase.deleteChallenger(DeleteChallengerCommand.of(
            challenger.getId(),
            "잘못 생성"
        ));

        assertThat(challengerRepository.findById(challenger.getId())).isEmpty();
        assertThat(pointRepository.count()).isZero();
    }
}
