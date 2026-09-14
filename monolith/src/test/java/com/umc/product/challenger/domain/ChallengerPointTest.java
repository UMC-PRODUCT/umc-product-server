package com.umc.product.challenger.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.common.domain.enums.ChallengerPart;

@DisplayName("ChallengerPoint 도메인")
class ChallengerPointTest {

    @Test
    @DisplayName("pointValue가 없으면 PointType의 기본 점수를 사용한다")
    void pointValue가_없으면_PointType의_기본_점수를_사용한다() {
        Challenger challenger = Challenger.builder()
            .memberId(1L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(9L)
            .build();

        ChallengerPoint point = ChallengerPoint.create(challenger, PointType.BEST_WORKBOOK, "베스트 워크북");

        assertThat(point.getPointValue()).isEqualTo(PointType.BEST_WORKBOOK.getValue());
    }

    @Test
    @DisplayName("pointValue가 있으면 PointType보다 custom 점수를 우선한다")
    void pointValue가_있으면_PointType보다_custom_점수를_우선한다() {
        Challenger challenger = Challenger.builder()
            .memberId(1L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(9L)
            .build();

        ChallengerPoint point = ChallengerPoint.create(challenger, PointType.CUSTOM, -7, "운영진 조정");

        assertThat(point.getPointValue()).isEqualTo(-7.0);
    }

    @Test
    @DisplayName("상벌점 설명을 수정한다")
    void 상벌점_설명을_수정한다() {
        Challenger challenger = Challenger.builder()
            .memberId(1L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(9L)
            .build();
        ChallengerPoint point = ChallengerPoint.create(challenger, PointType.CUSTOM, 1, "기존 설명");

        point.updateDescription("새 설명");

        assertThat(point.getDescription()).isEqualTo("새 설명");
    }
}
