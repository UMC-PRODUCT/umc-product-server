package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProjectMatchingRoundTest {

    private static final Instant STARTS_AT = Instant.parse("2026-05-10T00:00:00Z");
    private static final Instant ENDS_AT = Instant.parse("2026-05-15T00:00:00Z");
    private static final Instant DECISION_DEADLINE = Instant.parse("2026-05-17T00:00:00Z");

    ProjectMatchingRound round;

    @BeforeEach
    void setUp() {
        round = ProjectMatchingRound.create(
            "기획-디자인 1차 매칭", null,
            MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
            STARTS_AT, ENDS_AT, DECISION_DEADLINE
        );
    }

    @Nested
    class validateIsMutableAt {

        @Test
        void startsAt_정각이면_정상_통과() {
            assertThatCode(() -> round.validateIsMutableAt(STARTS_AT))
                .doesNotThrowAnyException();
        }

        @Test
        void 진행_중이면_정상_통과() {
            Instant midPoint = Instant.parse("2026-05-12T12:00:00Z");

            assertThatCode(() -> round.validateIsMutableAt(midPoint))
                .doesNotThrowAnyException();
        }

        @Test
        void decisionDeadline_정각이면_정상_통과() {
            assertThatCode(() -> round.validateIsMutableAt(DECISION_DEADLINE))
                .doesNotThrowAnyException();
        }

        @Test
        void startsAt_이전이면_PROJECT_MATCHING_ROUND_LOCKED() {
            Instant beforeStart = STARTS_AT.minusSeconds(1);

            assertThatThrownBy(() -> round.validateIsMutableAt(beforeStart))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
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
}
