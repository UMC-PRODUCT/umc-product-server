package com.umc.product.test.application.service.qa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.application.port.in.command.dto.QaStepResult;
import com.umc.product.test.application.port.in.command.dto.QaStepResult.Status;

@DisplayName("QA 시딩 단계 실행 순서와 결과")
class QaSeedOrchestratorTest {

    private final QaDirectoryLoader directoryLoader = mock(QaDirectoryLoader.class);

    @Test
    @DisplayName("계정 단계가 실패하면 뒤 단계는 실행하지 않고 NOT_RUN으로 돌려준다")
    void accountsFailureBlocksOtherSteps() {
        List<QaSeedStep> ran = new ArrayList<>();
        QaSeedOrchestrator sut = orchestrator(step -> {
            ran.add(step);
            if (step == QaSeedStep.ACCOUNTS) {
                throw new IllegalStateException("계정 실패");
            }
            return new StepTally();
        });

        List<QaStepResult> results = sut.seedAll();

        assertThat(ran).containsExactly(QaSeedStep.ACCOUNTS);
        assertThat(results.getFirst().status()).isEqualTo(Status.FAILED);
        assertThat(results.getFirst().errorCode()).isEqualTo("IllegalStateException");
        assertThat(results.subList(1, results.size())).extracting(QaStepResult::status).containsOnly(Status.NOT_RUN);
    }

    @Test
    @DisplayName("계정 외 단계가 실패해도 다음 단계는 실행하고, 만든 것이 없으면 UNCHANGED다")
    void otherFailureDoesNotBlock() {
        QaSeedOrchestrator sut = orchestrator(step -> {
            StepTally tally = new StepTally();
            if (step == QaSeedStep.LEARNING) {
                throw new IllegalArgumentException("학습 실패");
            }
            if (step == QaSeedStep.ACCOUNTS) {
                tally.created();
            } else {
                tally.skipped();
            }
            return tally;
        });

        List<QaStepResult> results = sut.seedAll();

        assertThat(results).extracting(QaStepResult::step).containsExactly(QaSeedStep.values());
        assertThat(results).extracting(QaStepResult::status).startsWith(Status.APPLIED, Status.FAILED);
        assertThat(results.subList(2, results.size())).extracting(QaStepResult::status)
            .containsOnly(Status.UNCHANGED);
    }

    @Test
    @DisplayName("단계 실행기가 빠지면 기동할 때 실패한다")
    void missingRunnerFailsFast() {
        List<QaSeedStepRunner> runners = runners(step -> new StepTally());
        runners.removeLast();

        assertThatThrownBy(() -> new QaSeedOrchestrator(runners, directoryLoader))
            .isInstanceOf(IllegalStateException.class);
    }

    private QaSeedOrchestrator orchestrator(Function<QaSeedStep, StepTally> behavior) {
        return new QaSeedOrchestrator(runners(behavior), directoryLoader);
    }

    private static List<QaSeedStepRunner> runners(Function<QaSeedStep, StepTally> behavior) {
        return new ArrayList<>(Arrays.stream(QaSeedStep.values()).<QaSeedStepRunner>map(step -> new QaSeedStepRunner() {
            @Override
            public QaSeedStep step() {
                return step;
            }

            @Override
            public StepTally run(QaDirectory directory) {
                return behavior.apply(step);
            }
        }).toList());
    }
}
