package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectPermissionReason;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

class ProjectQuotaEditPolicyTest {
    private static final Instant NOW = Instant.parse("2026-11-13T03:00:00Z");
    private final ProjectQuotaEditPolicy sut = new ProjectQuotaEditPolicy(new ProjectQuotaEditProperties(null));

    @ParameterizedTest
    @CsvSource({"-101,true", "-100,false", "-50,false", "0,false", "100,false", "101,false", "100000,false"})
    void 전체_첫_시작부터_3차_종료후에도_모든_파트를_계속_잠근다(long offset, boolean allowed) {
        // given
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60),
            round(MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, 50, 80, 100));

        // when & then
        for (ChallengerPart part : ChallengerPart.values()) {
            var result = sut.evaluate(part, rounds, NOW.plusSeconds(offset));
            assertThat(result.capability().allowed()).as(part.name()).isEqualTo(allowed);
            assertThat(result.editableAfter()).isNull();
        }
    }

    @Test
    void 개발이_먼저_시작해도_디자인과_PM을_함께_잠근다() {
        // given
        var rounds = List.of(round(MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -100, -80, -60),
            round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 50, 80, 100));

        // when & then
        assertThat(sut.evaluate(ChallengerPart.DESIGN, rounds, NOW).capability().allowed()).isFalse();
        assertThat(sut.evaluate(ChallengerPart.PLAN, rounds, NOW).capability().allowed()).isFalse();
    }

    @Test
    void 시작후_3차_일정이_없어도_재개시각_없이_잠근다() {
        // given
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60));

        // when
        var result = sut.evaluate(ChallengerPart.DESIGN, rounds, NOW);

        // then
        assertThat(result.capability().reasonCode())
            .isEqualTo(ProjectPermissionReason.PART_QUOTA_MATCHING_IN_PROGRESS.name());
        assertThat(result.editableAfter()).isNull();
    }

    @Test
    void 일반차수가_없어도_이미_랜덤매칭이_시작됐으면_잠근다() {
        // given
        var random = ProjectMatchingRound.create("랜덤", null, MatchingType.PLAN_DESIGN, MatchingPhase.RANDOM_MATCHING,
            1L, NOW.minusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(60), NOW.plusSeconds(600));

        // when & then
        assertThat(sut.evaluate(ChallengerPart.DESIGN, List.of(), NOW).capability().allowed()).isTrue();
        assertThat(sut.evaluate(ChallengerPart.DESIGN, List.of(random), NOW).capability().allowed()).isFalse();
    }

    @Test
    void 연장_시각_전이면_차수가_시작돼도_TO를_수정할_수_있다() {
        // given
        Instant openUntil = NOW.plusSeconds(3600);
        var extended = new ProjectQuotaEditPolicy(new ProjectQuotaEditProperties(openUntil));
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60));
        Instant now = openUntil.minusSeconds(1);

        // when
        var result = extended.evaluate(ChallengerPart.DESIGN, rounds, now);

        // then
        assertThat(result.capability().allowed()).isTrue();
        assertThatCode(() -> extended.validate(ChallengerPart.DESIGN, rounds, now)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({"0", "1"})
    void 연장_시각_정각부터_다시_잠근다(long offset) {
        // given
        Instant openUntil = NOW.plusSeconds(3600);
        var extended = new ProjectQuotaEditPolicy(new ProjectQuotaEditProperties(openUntil));
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60));
        Instant now = openUntil.plusSeconds(offset);

        // when
        var result = extended.evaluate(ChallengerPart.DESIGN, rounds, now);

        // then
        assertThat(result.capability().allowed()).isFalse();
        assertThatThrownBy(() -> extended.validate(ChallengerPart.DESIGN, rounds, now))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_PART_QUOTA_MATCHING_LOCKED);
    }

    @Test
    void 연장_설정이_비어_있으면_차수_시작부터_잠근다() {
        // given
        var rounds = List.of(round(MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60));

        // when & then
        assertThat(sut.evaluate(ChallengerPart.DESIGN, rounds, NOW).capability().allowed()).isFalse();
        assertThatThrownBy(() -> sut.validate(ChallengerPart.DESIGN, rounds, NOW))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_PART_QUOTA_MATCHING_LOCKED);
    }

    @ParameterizedTest
    @CsvSource(value = {"2026-11-13T12:00:00+09:00|2026-11-13T03:00:00Z", "''|"}, delimiter = '|')
    void 연장_시각은_오프셋_표기로_읽고_빈_값은_연장하지_않는다(String value, Instant expected) {
        // given
        var binder = new Binder(new MapConfigurationPropertySource(Map.of("app.project.quota-edit.open-until", value)));

        // when
        var properties = binder.bindOrCreate("app.project.quota-edit", ProjectQuotaEditProperties.class);

        // then
        assertThat(properties.openUntil()).isEqualTo(expected);
    }

    private ProjectMatchingRound round(MatchingType type, MatchingPhase phase, long starts, long ends, long deadline) {
        return ProjectMatchingRound.create("차수", null, type, phase, 1L, NOW.plusSeconds(starts), NOW.plusSeconds(ends),
            NOW.plusSeconds(deadline), NOW.plusSeconds(deadline + 3600));
    }
}
