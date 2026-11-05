package com.umc.product.test.adapter.in.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

import com.umc.product.test.application.port.in.command.SeedQaDataUseCase;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.application.port.in.command.dto.QaStepResult;
import com.umc.product.test.application.port.in.command.dto.QaStepResult.Status;

class QaSeedAutoRunnerTest {

    @Test
    @DisplayName("prod가 아니고 시딩과 QA 자동 실행이 모두 켜진 환경에서만 빈으로 등록된다")
    void registersOnlyWhenSeedAndAutoRunEnabled() {
        assertThat(QaSeedAutoRunner.class.getAnnotation(Profile.class).value()).contains("!prod");
        ConditionalOnProperty property = QaSeedAutoRunner.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(property.prefix()).isEqualTo("app.seed");
        assertThat(property.name()).containsExactlyInAnyOrder("enabled", "qa-auto-run");
        assertThat(property.havingValue()).isEqualTo("true");
    }

    @Test
    @DisplayName("QA 시딩 전체를 실행하고 단계별 결과를 돌려준다")
    void runsWholeQaSeeding() {
        // given
        SeedQaDataUseCase useCase = mock(SeedQaDataUseCase.class);
        QaStepResult accounts = new QaStepResult(QaSeedStep.values()[0], Status.UNCHANGED, 0, 593, Map.of(),
            null, null, 10);
        given(useCase.seedAll()).willReturn(List.of(accounts));

        // when
        List<QaStepResult> results = new QaSeedAutoRunner(useCase).run();

        // then
        assertThat(results).containsExactly(accounts);
    }

    @Test
    @DisplayName("QA 시딩이 예외를 던져도 기동을 깨지 않고 빈 결과를 돌려준다")
    void swallowsFailure() {
        // given
        SeedQaDataUseCase useCase = mock(SeedQaDataUseCase.class);
        given(useCase.seedAll()).willThrow(new IllegalStateException("11기가 없습니다"));

        // when
        List<QaStepResult> results = new QaSeedAutoRunner(useCase).run();

        // then
        assertThat(results).isEmpty();
    }
}
