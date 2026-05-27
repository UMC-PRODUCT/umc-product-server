package com.umc.product.curriculum.domain;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyCurriculumTest {

    private static final Instant START = Instant.parse("2024-03-01T00:00:00Z");
    private static final Instant END = Instant.parse("2024-03-07T23:59:59Z");

    private Curriculum curriculum;

    @BeforeEach
    void setUp() {
        curriculum = Curriculum.create(9L, ChallengerPart.SPRINGBOOT, "9기 스프링부트");
    }

    // ===== create =====

    @Nested
    @DisplayName("주차별 커리큘럼 생성")
    class Create {

        @Test
        void 정상적인_기간으로_생성에_성공한다() {
            WeeklyCurriculum wc = WeeklyCurriculum.create(curriculum, 1L, false, "1주차", START, END);

            assertThat(wc.getWeekNo()).isEqualTo(1L);
            assertThat(wc.isExtra()).isFalse();
            assertThat(wc.getTitle()).isEqualTo("1주차");
            assertThat(wc.getStartsAt()).isEqualTo(START);
            assertThat(wc.getEndsAt()).isEqualTo(END);
        }

        @Test
        void 부록_주차로_생성에_성공한다() {
            WeeklyCurriculum wc = WeeklyCurriculum.create(curriculum, 1L, true, "1주차 부록", START, END);

            assertThat(wc.isExtra()).isTrue();
        }

        @Test
        void 시작일과_종료일이_동일하면_생성에_성공한다() {
            // isAfter는 엄격 비교 → 동일 시각은 허용
            WeeklyCurriculum wc = WeeklyCurriculum.create(curriculum, 1L, false, "단일 시각", START, START);

            assertThat(wc.getStartsAt()).isEqualTo(wc.getEndsAt());
        }

        @Test
        void 시작일이_종료일_이후이면_예외가_발생한다() {
            Instant after = END.plusSeconds(1);

            assertThatThrownBy(() ->
                WeeklyCurriculum.create(curriculum, 1L, false, "잘못된 기간", after, END))
                .isInstanceOf(CurriculumDomainException.class)
                .extracting("baseCode")
                .isEqualTo(CurriculumErrorCode.INVALID_WEEKLY_CURRICULUM_PERIOD);
        }
    }

    // ===== update =====

    @Nested
    @DisplayName("주차별 커리큘럼 수정")
    class Update {

        private WeeklyCurriculum weeklyCurriculum;

        @BeforeEach
        void setUp() {
            weeklyCurriculum = WeeklyCurriculum.create(curriculum, 1L, false, "1주차", START, END);
        }

        @Test
        void 제목만_수정하면_나머지_값은_유지된다() {
            weeklyCurriculum.update(null, null, "수정된 제목", null, null);

            assertThat(weeklyCurriculum.getTitle()).isEqualTo("수정된 제목");
            assertThat(weeklyCurriculum.getWeekNo()).isEqualTo(1L);
            assertThat(weeklyCurriculum.isExtra()).isFalse();
            assertThat(weeklyCurriculum.getStartsAt()).isEqualTo(START);
            assertThat(weeklyCurriculum.getEndsAt()).isEqualTo(END);
        }

        @Test
        void 주차_번호를_수정할_수_있다() {
            weeklyCurriculum.update(2L, null, null, null, null);

            assertThat(weeklyCurriculum.getWeekNo()).isEqualTo(2L);
        }

        @Test
        void 부록_여부를_수정할_수_있다() {
            weeklyCurriculum.update(null, true, null, null, null);

            assertThat(weeklyCurriculum.isExtra()).isTrue();
        }

        @Test
        void 종료일만_수정하면_시작일은_유지된다() {
            Instant newEnd = END.plusSeconds(86400);
            weeklyCurriculum.update(null, null, null, null, newEnd);

            assertThat(weeklyCurriculum.getStartsAt()).isEqualTo(START);
            assertThat(weeklyCurriculum.getEndsAt()).isEqualTo(newEnd);
        }

        @Test
        void 빈_문자열_제목은_기존_제목을_유지한다() {
            weeklyCurriculum.update(null, null, "", null, null);

            assertThat(weeklyCurriculum.getTitle()).isEqualTo("1주차");
        }

        @Test
        void 공백만_있는_제목은_기존_제목을_유지한다() {
            weeklyCurriculum.update(null, null, "   ", null, null);

            assertThat(weeklyCurriculum.getTitle()).isEqualTo("1주차");
        }

        @Test
        void 모든_필드가_null이면_아무것도_변경되지_않는다() {
            weeklyCurriculum.update(null, null, null, null, null);

            assertThat(weeklyCurriculum.getWeekNo()).isEqualTo(1L);
            assertThat(weeklyCurriculum.isExtra()).isFalse();
            assertThat(weeklyCurriculum.getTitle()).isEqualTo("1주차");
            assertThat(weeklyCurriculum.getStartsAt()).isEqualTo(START);
            assertThat(weeklyCurriculum.getEndsAt()).isEqualTo(END);
        }

        @Test
        void 날짜_수정_후_시작일이_종료일_이후면_예외가_발생한다() {
            Instant invalidStart = END.plusSeconds(1);

            assertThatThrownBy(() ->
                weeklyCurriculum.update(null, null, null, invalidStart, null))
                .isInstanceOf(CurriculumDomainException.class)
                .extracting("baseCode")
                .isEqualTo(CurriculumErrorCode.INVALID_WEEKLY_CURRICULUM_PERIOD);
        }

        @Test
        void 종료일을_시작일_이전으로_수정하면_예외가_발생한다() {
            Instant invalidEnd = START.minusSeconds(1);

            assertThatThrownBy(() ->
                weeklyCurriculum.update(null, null, null, null, invalidEnd))
                .isInstanceOf(CurriculumDomainException.class)
                .extracting("baseCode")
                .isEqualTo(CurriculumErrorCode.INVALID_WEEKLY_CURRICULUM_PERIOD);
        }
    }
}
