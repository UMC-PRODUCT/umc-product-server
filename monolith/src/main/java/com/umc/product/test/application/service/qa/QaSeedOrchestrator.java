package com.umc.product.test.application.service.qa;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.test.application.port.in.command.SeedQaDataUseCase;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.application.port.in.command.dto.QaStepResult;
import com.umc.product.test.application.port.in.command.dto.QaStepResult.Status;

import lombok.extern.slf4j.Slf4j;

/**
 * 단계마다 따로 커밋되므로 한 단계가 실패해도 앞 단계는 남는다.
 */
@Slf4j
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class QaSeedOrchestrator implements SeedQaDataUseCase {

    private final Map<QaSeedStep, QaSeedStepRunner> runners = new EnumMap<>(QaSeedStep.class);
    private final QaDirectoryLoader directoryLoader;

    QaSeedOrchestrator(List<QaSeedStepRunner> runners, QaDirectoryLoader directoryLoader) {
        for (QaSeedStepRunner runner : runners) {
            if (this.runners.put(runner.step(), runner) != null) {
                throw new IllegalStateException("QA 시딩 단계 실행기가 중복됐습니다: " + runner.step());
            }
        }
        for (QaSeedStep step : QaSeedStep.values()) {
            if (!this.runners.containsKey(step)) {
                throw new IllegalStateException("QA 시딩 단계 실행기가 없습니다: " + step);
            }
        }
        this.directoryLoader = directoryLoader;
    }

    @Override
    public List<QaStepResult> seedAll() {
        List<QaStepResult> results = new ArrayList<>();
        boolean blocked = false;
        for (QaSeedStep step : QaSeedStep.values()) {
            if (blocked) {
                results.add(QaStepResult.notRun(step));
                continue;
            }
            QaStepResult result = seed(step);
            results.add(result);
            blocked = step.blocksOthersOnFailure() && result.status() == Status.FAILED;
        }
        return results;
    }

    @Override
    public QaStepResult seed(QaSeedStep step) {
        long startedAt = System.currentTimeMillis();
        try {
            StepTally tally = runners.get(step).run(directoryLoader.load());
            Status status = !tally.failedByCode().isEmpty() ? Status.FAILED
                : tally.createdCount() > 0 ? Status.APPLIED : Status.UNCHANGED;
            return new QaStepResult(step, status, tally.createdCount(), tally.skippedCount(), tally.failedByCode(),
                null, null, System.currentTimeMillis() - startedAt);
        } catch (RuntimeException e) {
            log.warn("QA 시딩 단계 실패: step={}", step, e);
            return new QaStepResult(step, Status.FAILED, 0, 0, Map.of(), SeedFailureCode.of(e), e.getMessage(),
                System.currentTimeMillis() - startedAt);
        }
    }
}
