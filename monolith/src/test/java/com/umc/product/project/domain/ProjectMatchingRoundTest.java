package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

class ProjectMatchingRoundTest {

    private static final Instant STARTS_AT = Instant.parse("2026-05-10T00:00:00Z");
    private static final Instant ENDS_AT = Instant.parse("2026-05-15T00:00:00Z");
    private static final Instant DECISION_DEADLINE = Instant.parse("2026-05-17T00:00:00Z");
    private static final Instant RESULT_PUBLISH_AT = Instant.parse("2026-05-17T02:00:00Z");

    ProjectMatchingRound round;

    @BeforeEach
    void setUp() {
        round = ProjectMatchingRound.create(
            "기획-디자인 1차 매칭", null,
            MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
            STARTS_AT, ENDS_AT, DECISION_DEADLINE, RESULT_PUBLISH_AT
        );
    }

    @Nested
    class resultPublishAt {

        @ParameterizedTest
        @EnumSource(MatchingPhase.class)
        void 발표_시각이_결정_마감으로부터_5분_미만이면_거부한다(MatchingPhase phase) {
            // given
            Instant endsAt = phase == MatchingPhase.FOURTH ? DECISION_DEADLINE : ENDS_AT;

            // when & then
            assertThatThrownBy(() -> ProjectMatchingRound.validateSchedule(
                MatchingType.PLAN_DESIGN, phase,
                STARTS_AT, endsAt, DECISION_DEADLINE, DECISION_DEADLINE.plusSeconds(300).minusNanos(1)
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
        }

        @ParameterizedTest
        @EnumSource(MatchingPhase.class)
        void 발표_시각이_결정_마감으로부터_정확히_5분_후이면_허용한다(MatchingPhase phase) {
            // given
            Instant endsAt = phase == MatchingPhase.FOURTH ? DECISION_DEADLINE : ENDS_AT;

            // when & then
            assertThatCode(() -> ProjectMatchingRound.validateSchedule(
                MatchingType.PLAN_DESIGN, phase,
                STARTS_AT, endsAt, DECISION_DEADLINE, DECISION_DEADLINE.plusSeconds(300)
            )).doesNotThrowAnyException();
        }

        @Test
        void 발표_시각이_선발_마감과_같으면_거부한다() {
            // when & then
            assertThatThrownBy(() -> ProjectMatchingRound.validateDates(
                STARTS_AT, ENDS_AT, DECISION_DEADLINE, DECISION_DEADLINE
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
        }

        @Test
        void 발표_시각이_선발_마감보다_빠르면_거부한다() {
            // when & then
            assertThatThrownBy(() -> ProjectMatchingRound.validateDates(
                STARTS_AT, ENDS_AT, DECISION_DEADLINE, DECISION_DEADLINE.minusSeconds(1)
            ))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_INVALID_PERIOD);
        }

        @Test
        void 잘못된_발표_시각으로_수정하면_기존_값을_유지한다() {
            // when & then
            assertThatThrownBy(() -> round.update(
                "수정한 이름", null, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST,
                STARTS_AT, ENDS_AT, DECISION_DEADLINE, DECISION_DEADLINE.plusSeconds(299)
            )).isInstanceOf(ProjectDomainException.class);
            assertThat(round.getName()).isEqualTo("기획-디자인 1차 매칭");
            assertThat(round.getResultPublishAt()).isEqualTo(RESULT_PUBLISH_AT);
        }
    }

    @Nested
    class isResultPublishedAt {

        @Test
        void 자동_선발이_완료되면_발표_시각_정각부터_공개한다() {
            // given
            round.executeAutoDecision(null);

            // when & then
            assertThat(round.isResultPublishedAt(RESULT_PUBLISH_AT)).isFalse();
            round.publishResult(RESULT_PUBLISH_AT);
            assertThat(round.isResultPublishedAt(RESULT_PUBLISH_AT)).isTrue();
        }

        @Test
        void 발표_시각이_지나도_자동_선발이_완료된_후에_공개한다() {
            // given
            Instant afterPublishAt = RESULT_PUBLISH_AT.plusSeconds(600);

            // when & then
            assertThat(round.isResultPublishedAt(afterPublishAt)).isFalse();
            round.executeAutoDecision(null);
            assertThat(round.isResultPublishedAt(afterPublishAt)).isFalse();
            round.publishResult(afterPublishAt);
            assertThat(round.isResultPublishedAt(afterPublishAt)).isTrue();
        }

        @Test
        void 발표_시각이_없는_기존_차수는_결정_마감_경과_후_공개한다() {
            // given
            ProjectMatchingRound existingRound = ProjectMatchingRound.create(
                "기획-디자인 1차 매칭", null,
                MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
                STARTS_AT, ENDS_AT, DECISION_DEADLINE, null
            );

            // when & then
            assertThat(existingRound.isResultPublishedAt(DECISION_DEADLINE.minusSeconds(1))).isFalse();
            assertThat(existingRound.isResultPublishedAt(DECISION_DEADLINE)).isFalse();
            assertThat(existingRound.isResultPublishedAt(DECISION_DEADLINE.plusSeconds(1))).isTrue();
        }
    }

    @Nested
    class validateIsMutableAt {

        @Test
        void endsAt_직후면_정상_통과() {
            assertThatCode(() -> round.validateIsMutableAt(ENDS_AT.plusSeconds(1)))
                .doesNotThrowAnyException();
        }

        @Test
        void 결정_마감_전이면_정상_통과() {
            Instant midPoint = Instant.parse("2026-05-16T00:00:00Z");

            assertThatCode(() -> round.validateIsMutableAt(midPoint))
                .doesNotThrowAnyException();
        }

        @Test
        void decisionDeadline_정각이면_정상_통과() {
            assertThatCode(() -> round.validateIsMutableAt(DECISION_DEADLINE))
                .doesNotThrowAnyException();
        }

        @Test
        void 지원_진행_중이면_PROJECT_MATCHING_ROUND_NOT_ENDED() {
            Instant duringApplication = Instant.parse("2026-05-12T12:00:00Z");

            assertThatThrownBy(() -> round.validateIsMutableAt(duringApplication))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_ENDED);
        }

        @Test
        void endsAt_정각이면_아직_지원_중이라_PROJECT_MATCHING_ROUND_NOT_ENDED() {
            assertThatThrownBy(() -> round.validateIsMutableAt(ENDS_AT))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_ENDED);
        }

        @Test
        void decisionDeadline_경과시_PROJECT_MATCHING_ROUND_LOCKED() {
            Instant afterDeadline = DECISION_DEADLINE.plusSeconds(1);

            assertThatThrownBy(() -> round.validateIsMutableAt(afterDeadline))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
        }
    }

    @Nested
    class validateIsViewableAt {

        @Test
        void endsAt_직후면_정상_통과() {
            assertThatCode(() -> round.validateIsViewableAt(ENDS_AT.plusSeconds(1)))
                .doesNotThrowAnyException();
        }

        @Test
        void 결정_마감_경과_후에도_정상_통과() {
            assertThatCode(() -> round.validateIsViewableAt(DECISION_DEADLINE.plusSeconds(1)))
                .doesNotThrowAnyException();
        }

        @Test
        void 지원_진행_중이면_PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE() {
            Instant duringApplication = Instant.parse("2026-05-12T12:00:00Z");

            assertThatThrownBy(() -> round.validateIsViewableAt(duringApplication))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE);
        }

        @Test
        void endsAt_정각이면_아직_지원_중이라_PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE() {
            assertThatThrownBy(() -> round.validateIsViewableAt(ENDS_AT))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_APPLICANTS_NOT_VIEWABLE);
        }
    }
}
