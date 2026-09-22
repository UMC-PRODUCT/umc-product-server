package com.umc.product.challenger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;

@DisplayName("ChallengerPoint 도메인")
class ChallengerPointTest {

    @ParameterizedTest
    @EnumSource(value = PointType.class, names = "CUSTOM", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("고정 유형의 점수를 생략하면 서버 기본 배점을 사용한다")
    void 고정_유형의_점수를_생략하면_서버_기본_배점을_사용한다(PointType type) {
        // given
        Challenger challenger = challenger();

        // when
        ChallengerPoint point = ChallengerPoint.create(challenger, type, null, "기본 배점");

        // then
        assertThat(point.getPointValue()).isEqualTo(type.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = PointType.class, names = {"CUSTOM", "BEST_WORKBOOK"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("고정 유형의 기본 배점을 명시한 기존 요청을 허용한다")
    void 고정_유형의_기본_배점을_명시한_기존_요청을_허용한다(PointType type) {
        // given
        Challenger challenger = challenger();
        int value = (int)type.getValue();

        // when
        ChallengerPoint point = ChallengerPoint.create(challenger, type, value, "명시한 기본 배점");

        // then
        assertThat(point.getPointValue()).isEqualTo(type.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = PointType.class, names = "CUSTOM", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("고정 유형의 배점을 다른 값으로 변경할 수 없다")
    void 고정_유형의_배점을_다른_값으로_변경할_수_없다(PointType type) {
        // given
        Challenger challenger = challenger();
        int differentValue = (int)type.getValue() + 1;

        // when & then
        assertThatThrownBy(() -> ChallengerPoint.create(challenger, type, differentValue, "잘못된 배점"))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode").isEqualTo(ChallengerErrorCode.INVALID_POINT_VALUE);
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, 0, 3})
    @DisplayName("CUSTOM은 명시한 점수와 부호를 보존한다")
    void CUSTOM은_명시한_점수와_부호를_보존한다(int value) {
        // given
        Challenger challenger = challenger();

        // when
        ChallengerPoint point = ChallengerPoint.create(challenger, PointType.CUSTOM, value, "직접 부여");

        // then
        assertThat(point.getPointValue()).isEqualTo((double)value);
    }

    @Test
    @DisplayName("CUSTOM의 점수가 null이면 생성을 거절한다")
    void CUSTOM의_점수가_null이면_생성을_거절한다() {
        // given
        Challenger challenger = challenger();

        // when & then
        assertThatThrownBy(() -> ChallengerPoint.create(challenger, PointType.CUSTOM, null, "점수 누락"))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode").isEqualTo(ChallengerErrorCode.CUSTOM_POINT_VALUE_REQUIRED);
    }

    @Test
    @DisplayName("점수 없는 생성 메서드로 CUSTOM 검증을 우회할 수 없다")
    void 점수_없는_생성_메서드로_CUSTOM_검증을_우회할_수_없다() {
        // given
        Challenger challenger = challenger();

        // when & then
        assertThatThrownBy(() -> ChallengerPoint.create(challenger, PointType.CUSTOM, "점수 누락"))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode").isEqualTo(ChallengerErrorCode.CUSTOM_POINT_VALUE_REQUIRED);
    }

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

    private Challenger challenger() {
        return Challenger.builder()
            .memberId(1L)
            .part(ChallengerPart.SPRINGBOOT)
            .gisuId(99L)
            .build();
    }
}
